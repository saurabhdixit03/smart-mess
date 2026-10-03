import api from "@/lib/api";

import type {
  ApiResponse,
} from "@/features/auth/types/auth.types";

import type {
  MessRegistrationLinkResponse,
} from "../types/messRegistration.types";

export const messRegistrationApi = {
  getRegistrationLink() {
    return api.get<
      ApiResponse<MessRegistrationLinkResponse>
    >(
      "/mess/registration-link"
    );
  },
};