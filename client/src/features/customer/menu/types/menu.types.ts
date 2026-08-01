export type MealSession = "LUNCH" | "DINNER";

export interface Menu {
    menuId: number;
    menuDate: string;
    mealSession: MealSession;

    sabjiOne: string;
    sabjiTwo: string | null;
    dal: string | null;
    rice: string | null;
    sweet: string | null;

    createdAt: string;
    updatedAt: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    path: string;
    data: T;
}

export type TodayMenusResponse = ApiResponse<Menu[]>;