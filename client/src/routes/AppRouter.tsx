import { BrowserRouter, Routes, Route } from "react-router-dom";

import OwnerLayout from "@/layouts/OwnerLayout";
import CustomerLayout from "@/layouts/CustomerLayout";

import NotFoundPage from "@/pages/NotFoundPage";
//import DesignSystemPage from "@/pages/DesignSystemPage";

import { CustomerPage } from "@/features/customers/pages";

import { MenuPage } from "@/features/menu";

import { DashboardPage } from "@/features/dashboard/pages";

import { MealRecordPage } from "@/features/meal-records/pages";

import { BillingPage } from "@/features/billing/pages";

import { PaymentPage } from "@/features/payments/pages";

import { SettingsPage } from "@/features/settings/pages";

import { InsightsPage } from "@/features/insights/pages";

import {
  OwnerLoginPage,
  OwnerRegistrationPage,
  CustomerRegistrationPage,
  CustomerLoginPage,
} from "@/features/auth/pages";

import ProtectedRoute from "./ProtectedRoute";
import PublicRoute from "./PublicRoute";

// Customer Portal imports

import { MenuPage as CustomerMenuPage } from "@/features/customer/menu";

import { MyMealsPage } from "@/features/customer/my-meals";

import {
  BillingPage as CustomerBillingPage,
} from "@/features/customer/billing";

import { ProfilePage } from "@/features/customer/profile";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>

        {/* ============================================================
            AUTH — PUBLIC ROUTES
            ============================================================ */}

        {/* Owner Authentication */}

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
        </Route>


        {/* Customer Authentication */}

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


        {/* ============================================================
            OWNER PORTAL — PROTECTED ROUTES
            Existing owner routes preserved exactly.
            ============================================================ */}

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
              path="payments"
              element={<PaymentPage />}
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


        {/* ============================================================
            CUSTOMER PORTAL — PROTECTED ROUTES
            Existing customer routes preserved exactly.
            ============================================================ */}

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
              path="profile"
              element={<ProfilePage />}
            />

          </Route>
        </Route>


        {/* ============================================================
            FALLBACK
            ============================================================ */}

        <Route
          path="*"
          element={<NotFoundPage />}
        />

      </Routes>
    </BrowserRouter>
  );
}