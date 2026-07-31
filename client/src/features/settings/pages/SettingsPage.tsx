import PageHeader from "@/components/common/ui/PageHeader";

import { SettingsForm } from "../components";

import { useSettings } from "../hooks";

export default function SettingsPage() {

  const {
    settings,
    loading,
    refreshSettings,
  } = useSettings();

  if (loading) {

    return (
      <div className="p-6">
        Loading settings...
      </div>
    );

  }

  return (

    <div className="space-y-6">

      <PageHeader
        title="Settings"
        description="Manage your mess payment settings."
      />

      <div
        className="
          rounded-2xl
          border
          border-[var(--color-border)]
          bg-white
          p-6
        "
      >

        <div className="mb-6">

          <h2 className="text-lg font-semibold">

            UPI Payment Settings

          </h2>

          <p className="mt-1 text-sm text-[var(--color-text-secondary)]">

            Configure the UPI ID and receiver name used for customer
            payments.

          </p>

        </div>

        <SettingsForm
          settings={settings}
          onSuccess={refreshSettings}
        />

      </div>

    </div>

  );

}