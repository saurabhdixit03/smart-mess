
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import type { DishIntelligence } from "../types";

type DishIntelligenceChartsProps = {
  dish: DishIntelligence | null;
};

const numberFormat = new Intl.NumberFormat("en-IN");

const ratingColors = [
  "var(--color-danger)",
  "var(--color-warning)",
  "var(--color-text-secondary)",
  "var(--color-success)",
  "var(--color-primary)",
];

export default function DishIntelligenceCharts({
  dish,
}: DishIntelligenceChartsProps) {
  if (!dish) {
    return (
      <div className="flex min-h-64 flex-col items-center justify-center rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 text-center">
        <h2 className="font-semibold text-[var(--color-text)]">
          No dishes available
        </h2>

        <p className="mt-2 text-sm text-[var(--color-text-secondary)]">
          Dish analytics will appear here once dishes are available.
        </p>
      </div>
    );
  }

  const ratingData = [
    { rating: "1 Star", count: dish.oneStarCount },
    { rating: "2 Stars", count: dish.twoStarCount },
    { rating: "3 Stars", count: dish.threeStarCount },
    { rating: "4 Stars", count: dish.fourStarCount },
    { rating: "5 Stars", count: dish.fiveStarCount },
  ];

  const totalRatings = ratingData.reduce(
    (total, item) => total + item.count,
    0
  );

  const hasRatings = totalRatings > 0;

  const averageRating =
    dish.averageRating !== null
      ? dish.averageRating.toFixed(1)
      : "—";

  const pieData = ratingData.filter((item) => item.count > 0);

  return (
    <div className="space-y-5">
      {/* Selected Dish Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-semibold text-[var(--color-text)]">
            {dish.dishName}
          </h3>

          <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
            Rating analytics and feedback distribution
          </p>
        </div>

        <span
          className={`rounded-full border px-2.5 py-1 text-xs font-medium ${
            dish.active
              ? "border-green-200 bg-green-50 text-green-700"
              : "border-[var(--color-border)] bg-[var(--color-surface-hover)] text-[var(--color-text-secondary)]"
          }`}
        >
          {dish.active ? "Active" : "Inactive"}
        </span>
      </div>

      {/* Summary Metrics */}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <p className="text-xs text-[var(--color-text-secondary)]">
            Average rating
          </p>

          <p className="mt-2 text-2xl font-bold text-[var(--color-text)]">
            {averageRating}
            <span className="ml-1 text-sm font-normal text-[var(--color-text-secondary)]">
              / 5
            </span>
          </p>
        </div>

        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <p className="text-xs text-[var(--color-text-secondary)]">
            Total ratings
          </p>

          <p className="mt-2 text-2xl font-bold tabular-nums text-[var(--color-text)]">
            {numberFormat.format(dish.totalRatings)}
          </p>
        </div>

        <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <p className="text-xs text-[var(--color-text-secondary)]">
            Written comments
          </p>

          <p className="mt-2 text-2xl font-bold tabular-nums text-[var(--color-text)]">
            {numberFormat.format(dish.totalComments)}
          </p>
        </div>
      </div>

      {hasRatings ? (
        <>
          {/* Charts Side by Side */}
          <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
            {/* Rating Distribution */}
            <div className="min-w-0 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 sm:p-5">
              <div className="mb-5">
                <h3 className="font-semibold text-[var(--color-text)]">
                  Rating Distribution
                </h3>

                <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
                  Number of customers for each rating level.
                </p>
              </div>

              <div className="h-[280px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={ratingData}
                    margin={{
                      top: 10,
                      right: 8,
                      left: -15,
                      bottom: 5,
                    }}
                  >
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke="var(--color-border)"
                      vertical={false}
                    />

                    <XAxis
                      dataKey="rating"
                      tick={{ fontSize: 11 }}
                      stroke="var(--color-text-secondary)"
                      interval={0}
                    />

                    <YAxis
                      allowDecimals={false}
                      tick={{ fontSize: 11 }}
                      stroke="var(--color-text-secondary)"
                    />

                    <Tooltip
                      cursor={{
                        fill: "var(--color-surface-hover)",
                      }}
                      formatter={(value) => [
                        numberFormat.format(Number(value)),
                        "Ratings",
                      ]}
                    />

                    <Bar
                      dataKey="count"
                      name="Ratings"
                      fill="var(--color-primary)"
                      radius={[6, 6, 0, 0]}
                      maxBarSize={45}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Rating Composition */}
            <div className="min-w-0 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 sm:p-5">
              <div className="mb-5">
                <h3 className="font-semibold text-[var(--color-text)]">
                  Rating Composition
                </h3>

                <p className="mt-1 text-xs text-[var(--color-text-secondary)]">
                  Share of each rating among all customer ratings.
                </p>
              </div>

              <div className="h-[280px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={pieData}
                      dataKey="count"
                      nameKey="rating"
                      cx="50%"
                      cy="46%"
                      innerRadius={55}
                      outerRadius={90}
                      paddingAngle={3}
                      stroke="none"
                    >
                      {pieData.map((item) => {
                        const index = ratingData.indexOf(item);

                        return (
                          <Cell
                            key={item.rating}
                            fill={ratingColors[index]}
                          />
                        );
                      })}
                    </Pie>

                    <Tooltip
                      formatter={(value, name) => [
                        `${numberFormat.format(Number(value))} (${(
                          (Number(value) / totalRatings) *
                          100
                        ).toFixed(1)}%)`,
                        name,
                      ]}
                    />

                    <Legend
                      verticalAlign="bottom"
                      height={36}
                      wrapperStyle={{ fontSize: "11px" }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        </>
      ) : (
        <div className="flex min-h-56 flex-col items-center justify-center rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 text-center">
          <h3 className="font-semibold text-[var(--color-text)]">
            No rating data available
          </h3>

          <p className="mt-2 max-w-md text-sm text-[var(--color-text-secondary)]">
            Charts will appear here once customers submit ratings for this dish.
          </p>
        </div>
      )}

      <p className="text-xs leading-5 text-[var(--color-text-secondary)]">
        Analytics reflect recorded customer feedback. Individual customer
        comments are not displayed.
      </p>
    </div>
  );
}
