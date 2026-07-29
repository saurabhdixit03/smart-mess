import api from "@/lib/api";

import { MEAL_RECORD_API_ENDPOINT } from "../constants/meal-record.constants";

import type {
  CollectionQueueResponse,
  CreateMealRecordRequest,
  MealSession,
  TodayMealRecordResponse,
} from "../types";
export async function getCollectionQueue(
  mealSession: MealSession
): Promise<CollectionQueueResponse> {

  return api.get<CollectionQueueResponse>(
    `${MEAL_RECORD_API_ENDPOINT}/collection-queue?mealSession=${mealSession}`
  );

}

export async function createMealRecord(
  payload: CreateMealRecordRequest
) {

  return api.post(
  MEAL_RECORD_API_ENDPOINT,
  payload
);

}

// for history table 

export async function getTodayMealRecords(
  mealSession: MealSession
): Promise<TodayMealRecordResponse> {

  return api.get<TodayMealRecordResponse>(
    `${MEAL_RECORD_API_ENDPOINT}/today?mealSession=${mealSession}`
  );

}