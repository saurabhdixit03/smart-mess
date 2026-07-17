import { Button } from "@/components/common/ui";

export default function DesignSystemPage() {
  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold">Design System</h1>

      <section className="space-y-4">
        <h2 className="text-lg font-semibold">Buttons</h2>

        <div className="flex flex-wrap gap-4">
          <Button>Primary</Button>

          <Button variant="secondary">
            Secondary
          </Button>

          <Button variant="outline">
            Outline
          </Button>

          <Button variant="danger">
            Danger
          </Button>
        </div>

        <div className="flex flex-wrap gap-4">
          <Button size="sm">Small</Button>

          <Button size="md">Medium</Button>

          <Button size="lg">Large</Button>
        </div>

        <Button fullWidth>
          Full Width Button
        </Button>

        <Button disabled>
          Disabled
        </Button>
      </section>
    </div>
  );
}