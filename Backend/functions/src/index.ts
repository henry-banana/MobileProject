import * as functions from "firebase-functions";
import express = require("express");
import { createNestApp } from "./main";

// Cache Express instance to avoid cold starts
let expressApp: express.Express | null = null;

async function getExpressApp(): Promise<express.Express> {
  if (!expressApp) {
    expressApp = express();
    await createNestApp(expressApp);
  }
  return expressApp;
}

// Export the Cloud Function
export const api = functions.https.onRequest(async (request, response) => {
  const app = await getExpressApp();
  app(request, response);
});
