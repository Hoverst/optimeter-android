import express from "express";
import cors from "cors";
import homesRouter from "./routes/homes";
import readingsRouter from "./routes/readings";

const app = express();
const port = process.env.PORT || 4000;

app.use(cors());
app.use(express.json());

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

app.use("/homes", homesRouter);
app.use("/readings", readingsRouter);

app.listen(port, () => {
  console.log(`Optimeter backend listening on port ${port}`);
});

