import api from "@/lib/api";

import { MEAL_RECORD_API_ENDPOINT } from "../constants/meal-record.constants";

import type {
  CollectionCustomerResponse,
  CollectionQueueResponse,
  CreateMealRecordRequest,
  MealRecordResponse,
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

export async function searchCollectionCustomers(
  mealSession: MealSession,
  search: string
): Promise<CollectionCustomerResponse> {
  const params = new URLSearchParams({
    mealSession,
    search: search.trim(),
  });

  return api.get<CollectionCustomerResponse>(
    `${MEAL_RECORD_API_ENDPOINT}/collection-customers?${params.toString()}`
  );
}

export async function createMealRecord(
  payload: CreateMealRecordRequest
): Promise<MealRecordResponse> {
  return api.post<MealRecordResponse>(
    MEAL_RECORD_API_ENDPOINT,
    payload
  );
}

export async function getTodayMealRecords(
  mealSession: MealSession
): Promise<TodayMealRecordResponse> {
  return api.get<TodayMealRecordResponse>(
    `${MEAL_RECORD_API_ENDPOINT}/today?mealSession=${mealSession}`
  );
}