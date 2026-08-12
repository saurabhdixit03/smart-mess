import MealRecordCard from "./MealRecordCard";

import type {
  CollectionQueueItem,
} from "../types";

type MealRecordQueueProps = {
  items: CollectionQueueItem[];

  onRecord: (
    item: CollectionQueueItem
  ) => void;
};

export default function MealRecordQueue({
  items,
  onRecord,
}: MealRecordQueueProps) {

  return (

    <div
      className="
  flex
  gap-4
  overflow-x-auto
  px-1
  py-2
"
    >

      {items.map((item) => (

        <MealRecordCard
          key={item.customerId}
          item={item}
          onRecord={onRecord}
        />

      ))}

    </div>

  );

}