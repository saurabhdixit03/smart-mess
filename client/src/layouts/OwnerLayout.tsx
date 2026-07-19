import { Outlet } from "react-router-dom";

import Header from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";

export default function OwnerLayout() {
  return (
    <>
      <Header />

      <div
        style={{
          display: "flex",
          minHeight: "calc(100vh - 64px)",
        }}
      >
        <Sidebar />

        <main
          style={{
            flex: 1,
            padding: "24px",
          }}
        >
          <Outlet />
        </main>
      </div>
    </>
  );
}