import {
  Home,
  Users,
  UtensilsCrossed,
  ClipboardList,
  HandPlatter,
  Receipt,
  Wallet,
  BarChart3,
  Settings,
  User,
} from "lucide-react";

import type { LucideIcon } from "lucide-react";

export type NavigationItem = {
  label: string;
  title: string;
  path: string;
  icon: LucideIcon;
};

import { ROUTES } from "@/constants/routes";

export const ownerNavigation: NavigationItem[] = [
  {
    label: "Dashboard",
    title: "Dashboard",
    path: ROUTES.DASHBOARD,
    icon: Home,
  },
  {
    label: "Customers",
    title: "Customer Management",
    path: ROUTES.CUSTOMERS,
    icon: Users,
  },
  {
    label: "Menu",
    title: "Menu Management",
    path: ROUTES.MENU,
    icon: UtensilsCrossed,
  },

  {
    label: "Live Dashboard",
    title: "Live Dashboard",
    path: ROUTES.MEALS,
    icon: ClipboardList,
  },
  {
    label: "Meal Collection",
    title: "Meal Records",
    path: ROUTES.MEAL_RECORDS,
    icon: HandPlatter,
  },
  {
    label: "Billing",
    title: "Billing",
    path: ROUTES.BILLING,
    icon: Receipt,
  },
  {
  label: "Payments",
  title: "Payment Management",
  path: ROUTES.PAYMENTS,
  icon: Wallet,
},
  {
  label: "Insights",
  title: "Business Insights",
  path: ROUTES.INSIGHTS,
  icon: BarChart3,
},
  {
    label: "Settings",
    title: "Settings",
    path: ROUTES.SETTINGS,
    icon: Settings,
  },
];

export const customerNavigation: NavigationItem[] = [
  {
    label: "Home",
    title: "Home",
    path: ROUTES.HOME,
    icon: Home,
  },
  {
    label: "Today's Menu",
    title: "Today's Menu",
    path: ROUTES.MENU,
    icon: UtensilsCrossed,
  },
  {
    label: "My Meals",
    title: "My Meals",
    path: ROUTES.MY_MEALS,
    icon: ClipboardList,
  },
  {
    label: "My Bills",
    title: "My Bills",
    path: ROUTES.MY_BILLS,
    icon: Receipt,
  },
  {
    label: "Profile",
    title: "My Profile",
    path: ROUTES.PROFILE,
    icon: User,
  },
];