import type { OwnerLoginResponse } from "../types/auth.types";

const AUTH_TOKEN_KEY = "smart_mess_access_token";
const OWNER_KEY = "smart_mess_owner";

export function saveAuthSession(
loginResponse: OwnerLoginResponse
): void {


localStorage.setItem(
    AUTH_TOKEN_KEY,
    loginResponse.accessToken
);

localStorage.setItem(
    OWNER_KEY,
    JSON.stringify({
        messOwnerId: loginResponse.messOwnerId,
        fullName: loginResponse.fullName,
        messName: loginResponse.messName,
    })
);


}

export function getAccessToken(): string | null {
return localStorage.getItem(AUTH_TOKEN_KEY);
}

export function getOwner(): {
messOwnerId: number;
fullName: string;
messName: string;
} | null {


const owner = localStorage.getItem(OWNER_KEY);

if (!owner) {
    return null;
}

return JSON.parse(owner);


}

export function clearAuthSession(): void {
localStorage.removeItem(AUTH_TOKEN_KEY);
localStorage.removeItem(OWNER_KEY);
}

export function isAuthenticated(): boolean {
return Boolean(getAccessToken());
}
