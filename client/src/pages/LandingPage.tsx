
import {
  ArrowRight,
  BarChart3,
  CheckCircle2,
  ClipboardList,
  CreditCard,
  LayoutDashboard,
  MessageCircle,
  Receipt,
  Store,
  Utensils,
  Users,
  UserRound,
} from "lucide-react";
import { Link } from "react-router-dom";

import Button from "@/components/common/ui/Button/Button";
import Card from "@/components/common/ui/Card/Card";

const workflowSteps = [
  {
    icon: Utensils,
    step: "01",
    title: "Publish the menu",
    description:
      "Share the day's lunch or dinner menu with your customers.",
  },
  {
    icon: MessageCircle,
    step: "02",
    title: "Customers respond",
    description:
      "Customers let you know whether they plan to have the meal.",
  },
  {
    icon: BarChart3,
    step: "03",
    title: "Plan with visibility",
    description:
      "See customer responses in one place and make better meal decisions.",
  },
];

const ownerFeatures = [
  {
    icon: Users,
    title: "Customer management",
    description:
      "Keep customer information and daily participation organized.",
  },
  {
    icon: Utensils,
    title: "Menu management",
    description:
      "Publish and manage daily lunch and dinner menus.",
  },
  {
    icon: LayoutDashboard,
    title: "Operational visibility",
    description:
      "See customer responses and important daily activity in one place.",
  },
  {
    icon: Receipt,
    title: "Billing & records",
    description:
      "Keep meal records, billing, payments, and related information organized.",
  },
];

const customerFeatures = [
  {
    icon: Utensils,
    title: "View the menu",
    description:
      "Know what's being served before deciding about your meal.",
  },
  {
    icon: MessageCircle,
    title: "Respond to meals",
    description:
      "Quickly communicate whether you plan to have the meal.",
  },
  {
    icon: ClipboardList,
    title: "Track your meals",
    description:
      "Keep a clear view of your meal participation and history.",
  },
  {
    icon: CreditCard,
    title: "View billing",
    description:
      "Access your billing and payment-related information digitally.",
  },
];

function SectionHeading({
  eyebrow,
  title,
  description,
}: {
  eyebrow: string;
  title: string;
  description: string;
}) {
  return (
    <div className="mx-auto max-w-2xl text-center">
      <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[var(--color-primary)]">
        {eyebrow}
      </p>

      <h2 className="mt-3 text-3xl font-bold tracking-tight text-[var(--color-text)] sm:text-4xl">
        {title}
      </h2>

      <p className="mt-4 text-base leading-7 text-[var(--color-text-secondary)]">
        {description}
      </p>
    </div>
  );
}

function FeatureCard({
  icon: Icon,
  title,
  description,
}: {
  icon: typeof Store;
  title: string;
  description: string;
}) {
  return (
    <Card className="h-full transition-all duration-200 hover:-translate-y-1 hover:shadow-[var(--shadow-lg)]">
      <Card.Body className="h-full">
        <div className="flex h-11 w-11 items-center justify-center rounded-[var(--radius-md)] bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
          <Icon size={21} strokeWidth={2} />
        </div>

        <h3 className="mt-5 text-lg font-semibold text-[var(--color-text)]">
          {title}
        </h3>

        <p className="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
          {description}
        </p>
      </Card.Body>
    </Card>
  );
}

export default function LandingPage() {
  return (
    <main className="min-h-screen bg-[var(--color-background)]">
      {/* ============================================================
          HEADER
          ============================================================ */}
      <header className="border-b border-[var(--color-border)] bg-[var(--color-background)]/95">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6 sm:px-8 lg:px-12">
          <Link
            to="/"
            className="flex items-center gap-2.5"
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white">
              <Utensils size={19} />
            </div>

            <span className="text-lg font-bold tracking-tight text-[var(--color-text)]">
              Smart Mess
            </span>
          </Link>

          <nav className="flex items-center gap-2 sm:gap-3">
            <Link to="/owner/login">
              <Button
                variant="outline"
                size="sm"
              >
                Owner Login
              </Button>
            </Link>

            <Link to="/customer/login">
              <Button
                size="sm"
                className="hidden sm:inline-flex"
              >
                Customer Login
              </Button>
            </Link>
          </nav>
        </div>
      </header>

      {/* ============================================================
          HERO
          ============================================================ */}
      <section className="relative overflow-hidden">
        <div className="mx-auto max-w-6xl px-6 pb-20 pt-20 sm:px-8 sm:pb-24 sm:pt-24 lg:px-12 lg:pb-28 lg:pt-28">
          <div className="mx-auto max-w-4xl text-center">

            <div className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-2 text-sm font-medium text-[var(--color-primary)] shadow-[var(--shadow-sm)]">
              <CheckCircle2 size={16} />
              <span>Built for everyday mess operations</span>
            </div>

            <h1 className="mt-7 text-4xl font-bold tracking-tight text-[var(--color-text)] sm:text-5xl lg:text-6xl">
              Plan meals with confidence.
              <span className="block text-[var(--color-primary)]">
                Run your mess with clarity.
              </span>
            </h1>

            <p className="mx-auto mt-6 max-w-2xl text-base leading-7 text-[var(--color-text-secondary)] sm:text-lg sm:leading-8">
              Smart Mess connects mess owners and customers through a simple
              digital workflow — helping owners understand expected meal
              demand before preparation and helping customers stay informed.
            </p>

            <div className="mt-9 flex flex-col justify-center gap-3 sm:flex-row">
              <Link to="/owner/login">
                <Button
                  size="lg"
                  className="w-full sm:w-auto"
                >
                  I'm a Mess Owner
                  <ArrowRight size={18} />
                </Button>
              </Link>

              <Link to="/customer/login">
                <Button
                  variant="outline"
                  size="lg"
                  className="w-full sm:w-auto"
                >
                  I'm a Customer
                  <ArrowRight size={18} />
                </Button>
              </Link>
            </div>

            <div className="mt-10 flex flex-wrap items-center justify-center gap-x-6 gap-y-3 text-sm text-[var(--color-text-secondary)]">
              <span className="flex items-center gap-2">
                <CheckCircle2
                  size={16}
                  className="text-[var(--color-success)]"
                />
                Real-time responses
              </span>

              <span className="flex items-center gap-2">
                <CheckCircle2
                  size={16}
                  className="text-[var(--color-success)]"
                />
                Better meal planning
              </span>

              <span className="flex items-center gap-2">
                <CheckCircle2
                  size={16}
                  className="text-[var(--color-success)]"
                />
                Simpler operations
              </span>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================================
          CORE WORKFLOW
          ============================================================ */}
      <section className="border-y border-[var(--color-border)] bg-[var(--color-surface)]">
        <div className="mx-auto max-w-6xl px-6 py-20 sm:px-8 lg:px-12">
          <SectionHeading
            eyebrow="How it works"
            title="From menu to meal planning"
            description="Smart Mess creates a simple interaction between the mess owner and customers before the food is prepared."
          />

          <div className="relative mt-14 grid gap-6 md:grid-cols-3">
            {workflowSteps.map((item, index) => {
              const Icon = item.icon;

              return (
                <div
                  key={item.step}
                  className="relative"
                >
                  <Card className="h-full">
                    <Card.Body className="h-full">
                      <div className="flex items-center justify-between">
                        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[var(--color-primary)] text-white">
                          <Icon size={21} />
                        </div>

                        <span className="text-3xl font-bold text-[var(--color-border)]">
                          {item.step}
                        </span>
                      </div>

                      <h3 className="mt-6 text-lg font-semibold text-[var(--color-text)]">
                        {item.title}
                      </h3>

                      <p className="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
                        {item.description}
                      </p>
                    </Card.Body>
                  </Card>

                  {index < workflowSteps.length - 1 && (
                    <div className="absolute right-0 top-1/2 z-10 hidden translate-x-1/2 -translate-y-1/2 md:block">
                      <ArrowRight
                        size={20}
                        className="text-[var(--color-border)]"
                      />
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* ============================================================
          OWNER FEATURES
          ============================================================ */}
      <section>
        <div className="mx-auto max-w-6xl px-6 py-20 sm:px-8 lg:px-12 lg:py-24">
          <div className="grid items-center gap-12 lg:grid-cols-[0.85fr_1.15fr]">

            <div>
              <div className="flex h-12 w-12 items-center justify-center rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white">
                <Store size={23} />
              </div>

              <p className="mt-6 text-sm font-semibold uppercase tracking-[0.18em] text-[var(--color-primary)]">
                For Mess Owners
              </p>

              <h2 className="mt-3 text-3xl font-bold tracking-tight text-[var(--color-text)] sm:text-4xl">
                More visibility into your daily operations.
              </h2>

              <p className="mt-5 text-base leading-7 text-[var(--color-text-secondary)]">
                Manage the everyday workflow of your mess in one place while
                using customer responses to make more informed meal planning
                decisions.
              </p>

              <Link
                to="/owner/login"
                className="mt-7 inline-block"
              >
                <Button>
                  Enter Owner Portal
                  <ArrowRight size={17} />
                </Button>
              </Link>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              {ownerFeatures.map((feature) => (
                <FeatureCard
                  key={feature.title}
                  {...feature}
                />
              ))}
            </div>

          </div>
        </div>
      </section>

      {/* ============================================================
          CUSTOMER FEATURES
          ============================================================ */}
      <section className="border-y border-[var(--color-border)] bg-[var(--color-surface)]">
        <div className="mx-auto max-w-6xl px-6 py-20 sm:px-8 lg:px-12 lg:py-24">
          <div className="grid items-center gap-12 lg:grid-cols-[1.15fr_0.85fr]">

            <div className="order-2 grid gap-4 sm:grid-cols-2 lg:order-1">
              {customerFeatures.map((feature) => (
                <FeatureCard
                  key={feature.title}
                  {...feature}
                />
              ))}
            </div>

            <div className="order-1 lg:order-2">
              <div className="flex h-12 w-12 items-center justify-center rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white">
                <UserRound size={23} />
              </div>

              <p className="mt-6 text-sm font-semibold uppercase tracking-[0.18em] text-[var(--color-primary)]">
                For Customers
              </p>

              <h2 className="mt-3 text-3xl font-bold tracking-tight text-[var(--color-text)] sm:text-4xl">
                Know what's on the menu. Stay in control of your meals.
              </h2>

              <p className="mt-5 text-base leading-7 text-[var(--color-text-secondary)]">
                Customers get a simple way to see the menu, communicate meal
                plans, keep track of participation, and access billing
                information.
              </p>

              <Link
                to="/customer/login"
                className="mt-7 inline-block"
              >
                <Button variant="outline">
                  Enter Customer Portal
                  <ArrowRight size={17} />
                </Button>
              </Link>
            </div>

          </div>
        </div>
      </section>

      {/* ============================================================
          VALUE PROPOSITION
          ============================================================ */}
      <section>
        <div className="mx-auto max-w-6xl px-6 py-20 sm:px-8 lg:px-12 lg:py-24">
          <div className="rounded-2xl bg-[var(--color-primary)] px-6 py-14 text-center shadow-[var(--shadow-lg)] sm:px-10 sm:py-16">
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-white/75">
              Smart Mess
            </p>

            <h2 className="mx-auto mt-4 max-w-2xl text-3xl font-bold tracking-tight text-white sm:text-4xl">
              A simpler way to make everyday mess decisions.
            </h2>

            <p className="mx-auto mt-5 max-w-2xl text-sm leading-7 text-white/80 sm:text-base">
              Give mess owners better visibility, give customers a clearer
              experience, and make the daily workflow more organized without
              changing how a mess naturally operates.
            </p>

            <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
              <Link to="/owner/login">
                <Button
                  variant="secondary"
                  size="lg"
                  className="w-full sm:w-auto"
                >
                  Owner Login
                  <ArrowRight size={18} />
                </Button>
              </Link>

              <Link to="/customer/login">
                <Button
                  size="lg"
                  className="w-full border border-white/30 bg-white/10 text-white hover:bg-white/20 sm:w-auto"
                >
                  Customer Login
                  <ArrowRight size={18} />
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================================
          FOOTER
          ============================================================ */}
      <footer className="border-t border-[var(--color-border)]">
        <div className="mx-auto flex max-w-6xl flex-col gap-3 px-6 py-8 text-sm text-[var(--color-text-secondary)] sm:flex-row sm:items-center sm:justify-between sm:px-8 lg:px-12">
          <div className="flex items-center gap-2 font-medium text-[var(--color-text)]">
            <Utensils size={16} className="text-[var(--color-primary)]" />
            Smart Mess
          </div>

          <p>
            © {new Date().getFullYear()} Smart Mess. Built for simpler mess
            operations.
          </p>
        </div>
      </footer>
    </main>
  );
}