import { NestFactory } from "@nestjs/core";
import { ExpressAdapter } from "@nestjs/platform-express";
import { AppModule } from "./app.module";
import express = require("express");

export async function createNestApp(
  expressInstance: express.Express
): Promise<void> {
  const adapter = new ExpressAdapter(expressInstance);

  const app = await NestFactory.create(AppModule, adapter);

  // Enable CORS for mobile clients
  app.enableCors({
    origin: true,
    credentials: true,
  });

  // Add global prefix if needed
  // app.setGlobalPrefix('api');

  await app.init();
}
