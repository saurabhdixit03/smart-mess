import api from "@/lib/api";

import type {
  ApiResponse,
  CreateMessClosureRequest,
  MessClosureResponse,
  UpdateMessClosureRequest,
} from "../types";

const MESS_CLOSURE_API_ENDPOINT =
  "/mess-closures";

export const messClosureApi = {
  getCurrentAndUpcomingClosures() {
    return api.get<
      ApiResponse<MessClosureResponse[]>
    >(
      MESS_CLOSURE_API_ENDPOINT
    );
  },

  getClosureHistory() {
    return api.get<
      ApiResponse<MessClosureResponse[]>
    >(
      `${MESS_CLOSURE_API_ENDPOINT}/history`
    );
  },

  createClosure(
    payload: CreateMessClosureRequest
  ) {
    return api.post<
      ApiResponse<MessClosureResponse>
    >(
      MESS_CLOSURE_API_ENDPOINT,
      payload
    );
  },

  updateClosure(
    closureId: number,
    payload: UpdateMessClosureRequest
  ) {
    return api.put<
      ApiResponse<MessClosureResponse>
    >(
      `${MESS_CLOSURE_API_ENDPOINT}/${closureId}`,
      payload
    );
  },

  deleteClosure(
    closureId: number
  ) {
    return api.delete<
      ApiResponse<null>
    >(
      `${MESS_CLOSURE_API_ENDPOINT}/${closureId}`
    );
  },
};