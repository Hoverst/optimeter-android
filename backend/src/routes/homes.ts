import { Router } from "express";
import { z } from "zod";
import { db } from "../config/firebase";
import { Home } from "../types";

const router = Router();

const createHomeSchema = z.object({
  name: z.string().min(1),
  address: z.string().min(1)
});

// GET /homes - list all homes
router.get("/", async (_req, res) => {
  try {
    const snapshot = await db.collection("homes").orderBy("createdAt", "desc").get();
    const homes: Home[] = snapshot.docs.map((doc) => {
      const data = doc.data();
      return {
        id: doc.id,
        name: data.name,
        address: data.address,
        createdAt: data.createdAt
      };
    });
    res.json(homes);
  } catch (err) {
    console.error("Error fetching homes", err);
    res.status(500).json({ error: "Failed to fetch homes" });
  }
});

// POST /homes - create a new home
router.post("/", async (req, res) => {
  try {
    const parsed = createHomeSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({ error: "Invalid payload", details: parsed.error.flatten() });
    }

    const now = new Date().toISOString();
    const docRef = await db.collection("homes").add({
      name: parsed.data.name,
      address: parsed.data.address,
      createdAt: now
    });

    const home: Home = {
      id: docRef.id,
      name: parsed.data.name,
      address: parsed.data.address,
      createdAt: now
    };

    res.status(201).json(home);
  } catch (err) {
    console.error("Error creating home", err);
    res.status(500).json({ error: "Failed to create home" });
  }
});

// DELETE /homes/:id - delete a home
router.delete("/:id", async (req, res) => {
  try {
    const { id } = req.params;
    await db.collection("homes").doc(id).delete();
    res.status(204).send();
  } catch (err) {
    console.error("Error deleting home", err);
    res.status(500).json({ error: "Failed to delete home" });
  }
});

export default router;

