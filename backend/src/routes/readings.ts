import { Router } from "express";
import { z } from "zod";
import { db } from "../config/firebase";
import { MeterReading, UtilityType } from "../types";

const router = Router();

const createReadingSchema = z.object({
  homeId: z.string().min(1),
  utility: z.enum(["electricity", "gas", "water"]),
  value: z.number().nonnegative(),
  readingAt: z.string().datetime()
});

// GET /readings?homeId=...&utility=...
router.get("/", async (req, res) => {
  try {
    const { homeId, utility } = req.query as { homeId?: string; utility?: UtilityType };

    if (!homeId) {
      return res.status(400).json({ error: "homeId query param is required" });
    }

    let query: FirebaseFirestore.Query = db
      .collection("readings")
      .where("homeId", "==", homeId)
      .orderBy("readingAt", "desc");

    if (utility) {
      query = query.where("utility", "==", utility);
    }

    const snapshot = await query.get();
    const readings: MeterReading[] = snapshot.docs.map((doc) => {
      const data = doc.data();
      return {
        id: doc.id,
        homeId: data.homeId,
        utility: data.utility,
        value: data.value,
        readingAt: data.readingAt,
        createdAt: data.createdAt
      };
    });

    res.json(readings);
  } catch (err) {
    console.error("Error fetching readings", err);
    res.status(500).json({ error: "Failed to fetch readings" });
  }
});

// POST /readings - create a new meter reading
router.post("/", async (req, res) => {
  try {
    const parsed = createReadingSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({ error: "Invalid payload", details: parsed.error.flatten() });
    }

    const now = new Date().toISOString();
    const docRef = await db.collection("readings").add({
      homeId: parsed.data.homeId,
      utility: parsed.data.utility,
      value: parsed.data.value,
      readingAt: parsed.data.readingAt,
      createdAt: now
    });

    const reading: MeterReading = {
      id: docRef.id,
      homeId: parsed.data.homeId,
      utility: parsed.data.utility,
      value: parsed.data.value,
      readingAt: parsed.data.readingAt,
      createdAt: now
    };

    res.status(201).json(reading);
  } catch (err) {
    console.error("Error creating reading", err);
    res.status(500).json({ error: "Failed to create reading" });
  }
});

// DELETE /readings/:id - delete a meter reading
router.delete("/:id", async (req, res) => {
  try {
    const { id } = req.params;
    await db.collection("readings").doc(id).delete();
    res.status(204).send();
  } catch (err) {
    console.error("Error deleting reading", err);
    res.status(500).json({ error: "Failed to delete reading" });
  }
});

export default router;

