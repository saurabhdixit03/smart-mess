import { useState } from "react";

import {
  Badge,
  Button,
  Card,
  DataTable,
  Input,
  Label,
  Modal,
  SectionTitle,
  Select,
  Textarea,
  
} from "@/components/common/ui";



export default function DesignSystemPage() {

  const [isModalOpen, setIsModalOpen] = useState(false);

  const customerColumns = [
  { key: "name", header: "Customer" },
  { key: "plan", header: "Plan" },
  { key: "status", header: "Status" },
] as const;

const customerData = [
  {
    name: "Rahul Patil",
    plan: "Full",
    status: "Active",
  },
  {
    name: "Sneha Kulkarni",
    plan: "Half",
    status: "Active",
  },
  {
    name: "Amit Joshi",
    plan: "Full",
    status: "Inactive",
  },
];

  return (
    <div className="mx-auto max-w-7xl space-y-10">
      <div className="space-y-2">
  <h1 className="text-3xl font-bold tracking-tight">
    🎨 Smart Mess Design System
  </h1>

  <p className="max-w-2xl text-[var(--color-text-secondary)]">
    Reusable UI components that ensure a consistent experience across the Smart Mess application.
  </p>
</div>

      <section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">
        <SectionTitle
  title="Buttons"
  description="Interactive actions used throughout the application."
/>

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

   <section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">
  <SectionTitle
    title="Cards"
    description="Reusable containers for dashboards, menus and analytics."
  />

  <div className="grid gap-6 lg:grid-cols-2">

    <Card>
      <Card.Header>
        <h3 className="text-lg font-semibold">
          Today's Lunch
        </h3>
      </Card.Header>

      <Card.Body>
        <ul className="space-y-2">
          <li>🥘 Paneer Masala</li>
          <li>🍛 Dal Fry</li>
          <li>🍚 Jeera Rice</li>
          <li>🫓 3 Rotis</li>
        </ul>
      </Card.Body>

      <Card.Footer className="flex justify-end">
        <Button>Publish Menu</Button>
      </Card.Footer>
    </Card>

    <Card>
      <Card.Header>
        <h3 className="text-lg font-semibold">
          Monthly Revenue
        </h3>
      </Card.Header>

      <Card.Body>
        <p className="text-4xl font-bold">
          ₹48,500
        </p>

        <p className="mt-2 text-sm text-green-600">
          ▲ 12% from last month
        </p>
      </Card.Body>
    </Card>

  </div>
</section>

<section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">

  <SectionTitle
    title="Inputs"
    description="Reusable form controls for menu management and customer information."
  />

  <div className="grid gap-6 lg:grid-cols-2">

    <Input
      placeholder="Menu Title"
    />

    <Input
      placeholder="Customer Name"
    />

    <Input
      placeholder="Disabled Input"
      disabled
    />

    <Input
      placeholder="Error State"
      error
    />

    <Input inputSize="sm" />

    <Input inputSize="md" />

    <Input inputSize="lg" />

    <Input
      placeholder="Full Width"
      fullWidth
    />

  </div>

</section>

<section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">

  <SectionTitle
    title="Textarea"
    description="Reusable multiline input for menus and announcements."
  />

  <div className="grid gap-6">

    <Textarea
      placeholder="Today's lunch menu..."
    />

    <Textarea
      placeholder="Announcement..."
      rows={6}
    />

    <Textarea
      placeholder="Error state"
      error
    />

    <Textarea
      placeholder="Disabled"
      disabled
    />

  </div>

</section>

<section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">

  <SectionTitle
    title="Labels"
    description="Consistent labels for forms across Smart Mess."
  />

  <div className="grid gap-6 md:grid-cols-2">

    <div>
      <Label>
        Customer Name
      </Label>

      <Input placeholder="Enter customer name" />
    </div>

    <div>
      <Label required>
        Menu Title
      </Label>

      <Input placeholder="Today's Lunch" />
    </div>

    <div>
      <Label>
        Announcement
      </Label>

      <Textarea placeholder="Type announcement..." />
    </div>

    <div>
      <Label required>
        Holiday Notice
      </Label>

      <Textarea placeholder="Holiday information..." />
    </div>

  </div>

</section>

<section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">

  <SectionTitle
    title="Select"
    description="Reusable dropdowns for forms and menu management."
  />

  <div className="grid gap-6 md:grid-cols-2">

    <div>
      <Label required>
        Meal Type
      </Label>

      <Select fullWidth defaultValue="">
        <option value="" disabled>
          Select meal
        </option>

        <option>Lunch</option>
        <option>Dinner</option>
      </Select>
    </div>

    <div>
      <Label>
        Meal Plan
      </Label>

      <Select fullWidth defaultValue="Half">
        <option>Half</option>
        <option>Full</option>
      </Select>
    </div>

    <div>
      <Label>
        Disabled
      </Label>

      <Select fullWidth disabled>
        <option>Disabled</option>
      </Select>
    </div>

    <div>
      <Label required>
        Error
      </Label>

      <Select fullWidth error defaultValue="">
        <option value="" disabled>
          Select option
        </option>

        <option>Option 1</option>
        <option>Option 2</option>
      </Select>
    </div>

  </div>

</section>

<section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">

  <SectionTitle
    title="Badge"
    description="Reusable status indicators for menus, billing and customers."
  />

  <div className="flex flex-wrap gap-3">

    <Badge>
      Published
    </Badge>

    <Badge variant="success">
      Active
    </Badge>

    <Badge variant="warning">
      Pending
    </Badge>

    <Badge variant="danger">
      Cancelled
    </Badge>

    <Badge variant="neutral">
      Draft
    </Badge>

  </div>

</section>



    <section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">

  <SectionTitle
    title="Modal"
    description="Reusable dialogs for menu publishing, editing and confirmations."
  />

  <Button onClick={() => setIsModalOpen(true)}>
    Open Modal
  </Button>

  <Modal
    open={isModalOpen}
    title="Publish Today's Menu"
    onClose={() => setIsModalOpen(false)}
    footer={
      <>
        <Button
          variant="secondary"
          onClick={() => setIsModalOpen(false)}
        >
          Cancel
        </Button>

        <Button>
          Publish
        </Button>
      </>
    }
  >
    <div className="space-y-4">

      <div>
        <Label required>
          Meal Type
        </Label>

        <Select fullWidth defaultValue="">
          <option value="" disabled>
            Select meal
          </option>

          <option>Lunch</option>
          <option>Dinner</option>
        </Select>
      </div>

      <div>
        <Label required>
          Menu Title
        </Label>

        <Input
          fullWidth
          placeholder="Enter menu title"
        />
      </div>

      <div>
        <Label>
          Description
        </Label>

        <Textarea
          fullWidth
          rows={4}
          placeholder="Describe today's menu..."
        />
      </div>

    </div>
  </Modal>

</section>


    <section className="space-y-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 shadow-sm">

  <SectionTitle
    title="Data Table"
    description="Reusable tables for customers, billing and meal records."
  />

  <DataTable
    columns={customerColumns}
    data={customerData}
  />

</section>


    </div>
  );
}