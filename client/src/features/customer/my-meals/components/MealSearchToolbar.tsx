import { Search } from "lucide-react";

import {
  Input,
  SearchToolbar,
} from "@/components/common/ui";

interface MealSearchToolbarProps {
  search: string;
  onSearchChange: (
    value: string
  ) => void;
}

export default function MealSearchToolbar({
  search,
  onSearchChange,
}: MealSearchToolbarProps) {
  return (
    <SearchToolbar>

      <SearchToolbar.Left>

        <Input
          fullWidth
          placeholder="Search meals by date or session..."
          value={search}
          onChange={(event) =>
            onSearchChange(
              event.target.value
            )
          }
          leftIcon={<Search size={18} />}
        />

      </SearchToolbar.Left>

    </SearchToolbar>
  );
}