import {
  BrowserRouter,
  Routes,
  Route,
} from "react-router-dom";

import OwnerLayout from "@/layouts/OwnerLayout";
import CustomerLayout from "@/layouts/CustomerLayout";

import LandingPage from "@/pages/LandingPage";
import NotFoundPage from "@/pages/NotFoundPage";

import { CustomerPage } from "@/features/owner/customers/pages";
import { MenuPage } from "@/features/owner/menu";
import { DashboardPage } from "@/features/owner/dashboard/pages";
import { MealRecordPage } from "@/features/owner/meal-records/pages";
import { BillingPage } from "@/features/owner/billing/pages";
import { SettingsPage } from "@/features/owner/settings/pages";
import { InsightsPage } from "@/features/owner/insights/pages";

import {
  OwnerLoginPage,
  OwnerRegistrationPage,
  CustomerRegistrationPage,
  CustomerLoginPage,
  ForgotPasswordPage,
  ResetPasswordPage,
} from "@/features/auth/pages";

import ProtectedRoute from "./ProtectedRoute";
import PublicRoute from "./PublicRoute";

import {
  MenuPage as CustomerMenuPage,
} from "@/features/customer/menu";

import { MyMealsPage } from "@/features/customer/my-meals";

import {
  BillingPage as CustomerBillingPage,
} from "@/features/customer/billing";

import { MessDetailsPage } from "@/features/customer/mess-details";
import { ProfilePage } from "@/features/customer/profile";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Owner authentication */}
        <Route
          element={
            <PublicRoute
              role="OWNER"
              redirectPath="/owner"
            />
          }
        >
          <Route
            path="/owner/login"
            element={<OwnerLoginPage />}
          />

          <Route
            path="/owner/register"
            element={<OwnerRegistrationPage />}
          />

          <Route
            path="/forgot-password"
            element={<ForgotPasswordPage />}
          />

          <Route
            path="/reset-password"
            element={<ResetPasswordPage />}
          />
        </Route>

        {/* Customer authentication */}
        <Route
          element={
            <PublicRoute
              role="CUSTOMER"
              redirectPath="/customer"
            />
          }
        >
          <Route
            path="/customer/register"
            element={<CustomerRegistrationPage />}
          />

          <Route
            path="/customer/login"
            element={<CustomerLoginPage />}
          />
        </Route>

        {/* Owner portal */}
        <Route
          element={
            <ProtectedRoute
              role="OWNER"
              loginPath="/owner/login"
            />
          }
        >
          <Route
            path="/owner"
            element={<OwnerLayout />}
          >
            <Route
              index
              element={<DashboardPage />}
            />

            <Route
              path="customers"
              element={<CustomerPage />}
            />

            <Route
              path="menu"
              element={<MenuPage />}
            />

            <Route
              path="meals"
              element={<DashboardPage />}
            />

            <Route
              path="meal-records"
              element={<MealRecordPage />}
            />

            <Route
              path="billing"
              element={<BillingPage />}
            />

            <Route
              path="settings"
              element={<SettingsPage />}
            />

            <Route
              path="insights"
              element={<InsightsPage />}
            />
          </Route>
        </Route>

        {/* Customer portal */}
        <Route
          element={
            <ProtectedRoute
              role="CUSTOMER"
              loginPath="/customer/login"
            />
          }
        >
          <Route
            path="/customer"
            element={<CustomerLayout />}
          >
            <Route
              index
              element={<CustomerMenuPage />}
            />

            <Route
              path="menu"
              element={<CustomerMenuPage />}
            />

            <Route
              path="my-meals"
              element={<MyMealsPage />}
            />

            <Route
              path="my-bills"
              element={<CustomerBillingPage />}
            />

            <Route
              path="mess-details"
              element={<MessDetailsPage />}
            />

            <Route
              path="profile"
              element={<ProfilePage />}
            />
          </Route>
        </Route>

        <Route
          path="/"
          element={<LandingPage />}
        />

        <Route
          path="*"
          element={<NotFoundPage />}
        />
      </Routes>
    </BrowserRouter>
  );
}