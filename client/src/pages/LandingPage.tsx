import {
  ArrowRight,
  BarChart3,
  CheckCircle2,
  ClipboardList,
  CreditCard,
  LayoutDashboard,
  Mail,
  MessageCircle,
  QrCode,
  Receipt,
  Store,
  UserCheck,
  UserRound,
  Users,
  Utensils,
  type LucideIcon,
} from "lucide-react";

import { Link } from "react-router-dom";

import Card from "@/components/common/ui/Card/Card";

const primaryLinkClass =
  "inline-flex min-h-11 items-center justify-center gap-2 rounded-xl " +
  "bg-[var(--color-primary)] px-5 py-3 text-sm font-semibold text-white " +
  "transition-opacity hover:opacity-90 focus-visible:outline-none " +
  "focus-visible:ring-2 focus-visible:ring-[var(--color-primary)] " +
  "focus-visible:ring-offset-2";

const secondaryLinkClass =
  "inline-flex min-h-11 items-center justify-center gap-2 rounded-xl " +
  "border border-[var(--color-border)] bg-[var(--color-surface)] " +
  "px-5 py-3 text-sm font-semibold text-[var(--color-text)] " +
  "transition-colors hover:bg-[var(--color-surface-hover)] " +
  "focus-visible:outline-none focus-visible:ring-2 " +
  "focus-visible:ring-[var(--color-primary)] focus-visible:ring-offset-2";

const setupSteps = [
  {
    icon: Store,
    step: "01",
    title: "Create your mess",
    description:
      "Register as an owner to get your own portal for customers, menus, meals, and billing.",
  },
  {
    icon: QrCode,
    step: "02",
    title: "Share your registration QR",
    description:
      "Display or share your mess's QR so customers register with the correct mess.",
  },
  {
    icon: UserCheck,
    step: "03",
    title: "Approve your customers",
    description:
      "Review new registrations. Approved customers receive an email with a login link.",
  },
];

const workflowSteps = [
  {
    icon: Utensils,
    step: "01",
    title: "Publish the menu",
    description:
      "Share the day's lunch or dinner menu with your approved customers.",
  },
  {
    icon: MessageCircle,
    step: "02",
    title: "Customers respond",
    description:
      "Customers let you know whether they plan to have the meal before the response cutoff.",
  },
  {
    icon: BarChart3,
    step: "03",
    title: "Plan with visibility",
    description:
      "See live responses for your mess and understand expected meal demand before preparation.",
  },
];

const ownerFeatures = [
  {
    icon: Users,
    title: "Customer management",
    description:
      "Review registrations, approve customers, and manage active and inactive accounts.",
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
      "See customer responses and important daily activity for your mess in one place.",
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
      "Keep a clear view of your recorded meals and history.",
  },
  {
    icon: CreditCard,
    title: "View billing",
    description:
      "Access your bills and payment-related information digitally.",
  },
];

type Feature = {
  icon: LucideIcon;
  title: string;
  description: string;
};

type Step = Feature & {
  step: string;
};

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
}: Feature) {
  return (
    <Card className="h-full transition-all duration-200 hover:-translate-y-1 hover:shadow-[var(--shadow-lg)] motion-reduce:transform-none motion-reduce:transition-none">
      <Card.Body className="h-full">
        <div className="flex h-11 w-11 items-center justify-center rounded-[var(--radius-md)] bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
          <Icon size={21} strokeWidth={2} aria-hidden="true" />
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

function StepsGrid({ steps }: { steps: readonly Step[] }) {
  return (
    <ol className="mt-12 grid gap-6 md:grid-cols-3">
      {steps.map((item) => {
        const Icon = item.icon;

        return (
          <li key={item.step}>
            <Card className="h-full">
              <Card.Body className="h-full">
                <div className="flex items-center justify-between">
                  <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[var(--color-primary)] text-white">
                    <Icon size={21} aria-hidden="true" />
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
          </li>
        );
      })}
    </ol>
  );
}

export default function LandingPage() {
  return (
    <main className="min-h-screen bg-[var(--color-background)]">
      {/* Header */}
      <header className="border-b border-[var(--color-border)] bg-[var(--color-background)]">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-4 sm:px-8 lg:px-12">
          <Link
            to="/"
            aria-label="Smart Mess home"
            className="flex items-center gap-2.5 rounded-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]"
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white">
              <Utensils size={19} aria-hidden="true" />
            </div>

            <span className="text-lg font-bold tracking-tight text-[var(--color-text)]">
              Smart Mess
            </span>
          </Link>

          <nav
            aria-label="Account navigation"
            className="flex items-center gap-2 sm:gap-3"
          >
            <Link
              to="/customer/login"
              className="inline-flex min-h-11 items-center rounded-lg px-2 text-sm font-medium text-[var(--color-text-secondary)] transition-colors hover:text-[var(--color-primary)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)] sm:px-3"
            >
              Customer Login
            </Link>

            <Link
              to="/owner/login"
              className={`${secondaryLinkClass} px-3 py-2`}
            >
              Owner Login
            </Link>
          </nav>
        </div>
      </header>

      {/* Hero */}
      <section>
        <div className="mx-auto max-w-6xl px-6 pb-16 pt-16 sm:px-8 sm:pb-20 sm:pt-20 lg:px-12 lg:pb-24 lg:pt-24">
          <div className="mx-auto max-w-4xl text-center">
            <div className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-2 text-sm font-medium text-[var(--color-primary)] shadow-[var(--shadow-sm)]">
              <CheckCircle2 size={16} aria-hidden="true" />
              <span>Built for everyday mess operations</span>
            </div>

            <h1 className="mt-7 text-4xl font-bold tracking-tight text-[var(--color-text)] sm:text-5xl lg:text-6xl">
              Plan meals with confidence.
              <span className="block text-[var(--color-primary)]">
                Run your mess with clarity.
              </span>
            </h1>

            <p className="mx-auto mt-6 max-w-2xl text-base leading-7 text-[var(--color-text-secondary)] sm:text-lg sm:leading-8">
              Know how many customers plan to eat before preparation.
              Bring daily menus, customer responses, meal records, and
              billing together in your own mess portal.
            </p>

            <div className="mt-9 flex flex-col justify-center gap-3 sm:flex-row">
              <Link
                to="/owner/register"
                className={primaryLinkClass}
              >
                Create Your Mess
                <ArrowRight size={18} aria-hidden="true" />
              </Link>

              <Link
                to="/owner/login"
                className={secondaryLinkClass}
              >
                Owner Login
              </Link>
            </div>

            <p className="mt-5 text-sm text-[var(--color-text-secondary)]">
              Already a customer?{" "}
              <Link
                to="/customer/login"
                className="rounded-sm font-semibold text-[var(--color-primary)] underline-offset-4 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]"
              >
                Log in to your mess
              </Link>
            </p>

            <div className="mt-10 flex flex-wrap items-center justify-center gap-x-6 gap-y-3 text-sm text-[var(--color-text-secondary)]">
              {[
                "Real-time responses",
                "Better meal planning",
                "Your mess's own workspace",
              ].map((benefit) => (
                <span
                  key={benefit}
                  className="flex items-center gap-2"
                >
                  <CheckCircle2
                    size={16}
                    aria-hidden="true"
                    className="text-[var(--color-success)]"
                  />
                  {benefit}
                </span>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Owner onboarding */}
      <section className="border-y border-[var(--color-border)] bg-[var(--color-surface)]">
        <div className="mx-auto max-w-6xl px-6 py-16 sm:px-8 lg:px-12 lg:py-20">
          <SectionHeading
            eyebrow="Getting started"
            title="Your mess. Your customers. One place."
            description="Set up your mess and bring your customers on board through your dedicated registration link."
          />

          <StepsGrid steps={setupSteps} />
        </div>
      </section>

      {/* Daily workflow */}
      <section>
        <div className="mx-auto max-w-6xl px-6 py-16 sm:px-8 lg:px-12 lg:py-20">
          <SectionHeading
            eyebrow="The daily workflow"
            title="From menu to meal planning"
            description="Smart Mess connects owners and customers before food preparation, helping everyone make clearer meal decisions."
          />

          <StepsGrid steps={workflowSteps} />
        </div>
      </section>

      {/* Owner features */}
      <section className="border-y border-[var(--color-border)] bg-[var(--color-surface)]">
        <div className="mx-auto max-w-6xl px-6 py-16 sm:px-8 lg:px-12 lg:py-20">
          <div className="grid items-center gap-10 lg:grid-cols-[0.85fr_1.15fr] lg:gap-12">
            <div>
              <div className="flex h-12 w-12 items-center justify-center rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white">
                <Store size={23} aria-hidden="true" />
              </div>

              <p className="mt-6 text-sm font-semibold uppercase tracking-[0.18em] text-[var(--color-primary)]">
                For Mess Owners
              </p>

              <h2 className="mt-3 text-3xl font-bold tracking-tight text-[var(--color-text)] sm:text-4xl">
                More visibility into your daily operations.
              </h2>

              <p className="mt-5 text-base leading-7 text-[var(--color-text-secondary)]">
                Manage your everyday workflow in one place. Use customer
                responses to plan meals, keep collection records, and
                stay on top of billing and payments.
              </p>

              <p className="mt-4 text-sm leading-6 text-[var(--color-text-secondary)]">
                Your portal shows your own mess's customers and activity.
                New registrations join daily operations only after approval.
              </p>

              <Link
                to="/owner/register"
                className={`${primaryLinkClass} mt-7`}
              >
                Create Your Mess
                <ArrowRight size={17} aria-hidden="true" />
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

      {/* Customer features and onboarding guidance */}
      <section id="customers">
        <div className="mx-auto max-w-6xl px-6 py-16 sm:px-8 lg:px-12 lg:py-20">
          <div className="grid items-center gap-10 lg:grid-cols-[1.15fr_0.85fr] lg:gap-12">
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
                <UserRound size={23} aria-hidden="true" />
              </div>

              <p className="mt-6 text-sm font-semibold uppercase tracking-[0.18em] text-[var(--color-primary)]">
                For Customers
              </p>

              <h2 className="mt-3 text-3xl font-bold tracking-tight text-[var(--color-text)] sm:text-4xl">
                Know what's on the menu. Stay in control of your meals.
              </h2>

              <p className="mt-5 text-base leading-7 text-[var(--color-text-secondary)]">
                See your mess's menu, communicate your meal plans,
                track recorded meals, and access your billing information.
              </p>

              <Link
                to="/customer/login"
                className={`${secondaryLinkClass} mt-7`}
              >
                Customer Login
                <ArrowRight size={17} aria-hidden="true" />
              </Link>

              <div className="mt-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
                <div className="flex items-center gap-2 text-[var(--color-text)]">
                  <QrCode
                    size={20}
                    aria-hidden="true"
                    className="shrink-0 text-[var(--color-primary)]"
                  />
                  <h3 className="font-semibold">
                    Joining a mess?
                  </h3>
                </div>

                <p className="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
                  Scan the registration QR displayed at your mess, or
                  ask the staff for its registration link. Check the
                  mess name on the page before submitting your details.
                </p>

                <div className="mt-4 flex items-start gap-2 border-t border-[var(--color-border)] pt-4">
                  <Mail
                    size={17}
                    aria-hidden="true"
                    className="mt-0.5 shrink-0 text-[var(--color-primary)]"
                  />

                  <p className="text-sm leading-6 text-[var(--color-text-secondary)]">
                    Your account needs owner approval. After approval,
                    we'll email you a login link. Sign in with the email
                    and password you used to register.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Final owner call to action */}
      <section>
        <div className="mx-auto max-w-6xl px-6 pb-16 sm:px-8 lg:px-12 lg:pb-20">
          <div className="rounded-2xl bg-[var(--color-primary)] px-6 py-12 text-center shadow-[var(--shadow-lg)] sm:px-10 sm:py-16">
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-white/75">
              Start with your mess
            </p>

            <h2 className="mx-auto mt-4 max-w-2xl text-3xl font-bold tracking-tight text-white sm:text-4xl">
              A simpler way to make everyday mess decisions.
            </h2>

            <p className="mx-auto mt-5 max-w-2xl text-sm leading-7 text-white/80 sm:text-base">
              Give your customers a clearer meal experience and bring
              your daily operations together—from menu responses to
              meal records and billing.
            </p>

            <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
              <Link
                to="/owner/register"
                className="inline-flex min-h-11 items-center justify-center gap-2 rounded-xl bg-white px-6 py-3 text-sm font-semibold text-[var(--color-primary)] transition-colors hover:bg-white/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--color-primary)]"
              >
                Create Your Mess
                <ArrowRight size={18} aria-hidden="true" />
              </Link>

              <Link
                to="/owner/login"
                className="inline-flex min-h-11 items-center justify-center gap-2 rounded-xl border border-white/40 px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-white/10 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--color-primary)]"
              >
                Owner Login
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-[var(--color-border)]">
        <div className="mx-auto flex max-w-6xl flex-col gap-5 px-6 py-8 text-sm text-[var(--color-text-secondary)] sm:px-8 lg:px-12">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-2 font-medium text-[var(--color-text)]">
              <Utensils
                size={16}
                aria-hidden="true"
                className="text-[var(--color-primary)]"
              />
              Smart Mess
            </div>

            <nav
              aria-label="Footer account links"
              className="flex flex-wrap gap-x-5 gap-y-3"
            >
              <Link
                to="/owner/register"
                className="hover:text-[var(--color-primary)] hover:underline"
              >
                Create Your Mess
              </Link>

              <Link
                to="/owner/login"
                className="hover:text-[var(--color-primary)] hover:underline"
              >
                Owner Login
              </Link>

              <Link
                to="/customer/login"
                className="hover:text-[var(--color-primary)] hover:underline"
              >
                Customer Login
              </Link>
            </nav>
          </div>

          <p>
            © {new Date().getFullYear()} Smart Mess. Built for simpler
            mess operations.
          </p>
        </div>
      </footer>
    </main>
  );
}