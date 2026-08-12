import { useMemo, useState } from "react";

import {
  Button,
  Card,
  Input,
  Modal,
  StatusBadge,
} from "@/components/common/ui";

import { Search } from "lucide-react";

import type { BillDetailResponse } from "../types";

type BillDetailsDialogProps = {
  open: boolean;
  bill: BillDetailResponse | null;
  loading: boolean;
  onClose: () => void;
};

function formatDate(date: string) {
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function formatDay(date: string) {
  return new Date(date).toLocaleDateString("en-IN", {
    day: "2-digit",
  });
}

function formatBillingMonth(
  month: number,
  year: number
) {
  return new Date(
    year,
    month - 1
  ).toLocaleDateString("en-IN", {
    month: "long",
    year: "numeric",
  });
}

function getBillStatusVariant(
  status: BillDetailResponse["billStatus"]
): "success" | "warning" | "info" {
  switch (status) {
    case "PAID":
      return "success";

    case "UNPAID":
    case "PAYMENT_PENDING":
      return "warning";

    default:
      return "info";
  }
}

export default function BillDetailsDialog({
  open,
  bill,
  loading,
  onClose,
}: BillDetailsDialogProps) {
  const [search, setSearch] =
    useState("");

  const groupedMeals =
    useMemo(() => {
      if (!bill) {
        return [];
      }

      const keyword =
        search.trim().toLowerCase();

      const filtered =
        bill.mealRecords.filter((meal) =>
          formatDate(meal.collectedAt)
            .toLowerCase()
            .includes(keyword)
        );

      const groups = new Map<
        string,
        typeof filtered
      >();

      filtered.forEach((meal) => {
        const key = formatDate(
          meal.collectedAt
        );

        if (!groups.has(key)) {
          groups.set(key, []);
        }

        groups.get(key)!.push(meal);
      });

      return Array.from(groups.entries());
    }, [bill, search]);

  return (
    <Modal
      open={open}
      title="Bill Details"
      size="xl"
      onClose={onClose}
      footer={
        <Button
          variant="outline"
          onClick={onClose}
        >
          Close
        </Button>
      }
    >
      {loading ? (
        <p>Loading...</p>
      ) : !bill ? (
        <p>No bill details found.</p>
      ) : (
        <div className="flex max-h-[85vh] flex-col gap-6">

          {/* Header */}

          <div>
            <h2 className="text-xl font-semibold">
              {bill.customerName}
            </h2>

            <p className="text-sm text-[var(--color-text-secondary)]">
              {formatBillingMonth(
                bill.billingMonth,
                bill.billingYear
              )}
            </p>
          </div>

          {/* Summary */}

          <Card>

            <Card.Body>

              <div className="grid grid-cols-3 gap-6">

                <div>

                  <p className="text-sm text-[var(--color-text-secondary)]">
                    Status
                  </p>

                  <div className="mt-2">
                    <StatusBadge
                      label={bill.billStatus}
                      variant={getBillStatusVariant(
                        bill.billStatus
                      )}
                    />
                  </div>

                </div>

                <div>

                  <p className="text-sm text-[var(--color-text-secondary)]">
                    Meals Served
                  </p>

                  <p className="mt-2 text-lg font-semibold">
                    {bill.mealRecordCount}
                  </p>

                </div>

                <div className="text-right">

                  <p className="text-sm text-[var(--color-text-secondary)]">
                    Total Amount
                  </p>

                  <p className="mt-2 text-xl font-bold">
                    ₹{bill.totalAmount}
                  </p>

                </div>

              </div>

            </Card.Body>

          </Card>

          {/* Search */}

          <Input
            fullWidth
            leftIcon={<Search size={18} />}
            placeholder="Search by date (e.g. 15 )"
            value={search}
            onChange={(e) =>
              setSearch(e.target.value)
            }
          />

          {/* Scrollable Records */}

          <div className="flex-1 overflow-y-auto rounded-xl border border-[var(--color-border)]">

            <table className="min-w-full">

              <thead className="sticky top-0 bg-[var(--color-surface)]">

                <tr className="border-b border-[var(--color-border)]">

                  <th className="w-20 px-4 py-3 text-left text-sm font-semibold">
                    Date
                  </th>

                  <th className="px-4 py-3 text-left text-sm font-semibold">
                    Session
                  </th>

                  <th className="px-4 py-3 text-left text-sm font-semibold">
                    Meal
                  </th>

                  <th className="px-4 py-3 text-center text-sm font-semibold">
                    Extra Rotis
                  </th>

                  <th className="px-4 py-3 text-right text-sm font-semibold">
                    Amount
                  </th>

                </tr>

              </thead>

              <tbody>

                {groupedMeals.map(
                  ([, meals]) =>

                    meals.map(
                      (meal, index) => (

                        <tr
                          key={meal.mealRecordId}
                          className="border-b border-[var(--color-border)]"
                        >

                          {index === 0 && (

                            <td
                              rowSpan={
                                meals.length
                              }
                              className="align-top px-4 py-4 font-semibold"
                            >
                              {formatDay(
                                meal.collectedAt
                              )}
                            </td>

                          )}

                          <td className="px-4 py-3">

                            <StatusBadge
                              label={
                                meal.mealSession
                              }
                              variant={
                                meal.mealSession ===
                                "LUNCH"
                                  ? "lunch"
                                  : "dinner"
                              }
                            />

                          </td>

                          <td className="px-4 py-3">

                            <StatusBadge
                              label={
                                meal.mealOption
                              }
                              variant={
                                meal.mealOption ===
                                "FULL"
                                  ? "full"
                                  : "half"
                              }
                            />

                          </td>

                          <td className="px-4 py-3 text-center">
                            {
                              meal.extraRotiCount
                            }
                          </td>

                          <td className="px-4 py-3 text-right font-semibold">
                            ₹
                            {meal.totalAmount}
                          </td>

                        </tr>

                      )
                    )
                )}

              </tbody>

            </table>

          </div>

          <div className="text-right text-sm text-[var(--color-text-secondary)]">
            Generated on{" "}
            {formatDate(
              bill.generatedAt
            )}
          </div>

        </div>
      )}
    </Modal>
  );
}