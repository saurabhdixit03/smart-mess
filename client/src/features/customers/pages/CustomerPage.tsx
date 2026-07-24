import { useEffect, useMemo, useState } from "react";

import { toast } from "sonner";

import {
  CustomerForm,
  CustomerTable,
} from "../components";

import { useCustomers } from "../hooks/useCustomers";
import { customerApi } from "../api/customer.api";

import type { CustomerResponse } from "../types/customer.types";

import Button from "@/components/common/ui/Button/Button";
import Input from "@/components/common/ui/Input/Input";
import Modal from "@/components/common/ui/Modal/Modal";

import PageHeader from "@/components/common/ui/PageHeader";

import StatsCard from "@/components/common/ui/StatsCard";
import SearchToolbar from "@/components/common/ui/SearchToolbar";

import {
    Plus, 
    Search,
    Users,
    UserCheck,
    UserX,
} from "lucide-react";

const CustomerPage = () => {

  const {
    customers,
    loading,
    error,
    fetchCustomers,
  } = useCustomers();

  // -------------------------
  // Modal State
  // -------------------------

  const [isFormOpen, setIsFormOpen] =
    useState(false);

  const [selectedCustomer, setSelectedCustomer] =
    useState<CustomerResponse | null>(null);

  const [customerToDeactivate, setCustomerToDeactivate] =
    useState<CustomerResponse | null>(null);

  // -------------------------
  // Search
  // -------------------------

  const [search, setSearch] =
    useState("");

  // -------------------------
  // Pagination
  // -------------------------

  const [currentPage, setCurrentPage] =
    useState(1);

  const [rowsPerPage, setRowsPerPage] =
    useState(10);

  // -------------------------
  // Filter
  // -------------------------

  const filteredCustomers = useMemo(() => {

    const keyword =
      search.trim().toLowerCase();

    if (!keyword) {
      return customers;
    }

    return customers.filter((customer) => {

      return (

        customer.fullName
          .toLowerCase()
          .includes(keyword)

        ||

        customer.mobileNumber
          .includes(keyword)

        ||

        (customer.email ?? "")
          .toLowerCase()
          .includes(keyword)

      );

    });

  }, [customers, search]);

  // -------------------------
  // Search always starts
  // from page 1
  // -------------------------

  useEffect(() => {

    setCurrentPage(1);

  }, [search]);

  // -------------------------
  // Pagination
  // -------------------------

  const totalCustomers =
    filteredCustomers.length;

  const totalPages =
    Math.max(
      1,
      Math.ceil(
        totalCustomers / rowsPerPage
      )
    );

  const paginatedCustomers =
    useMemo(() => {

      const start =
        (currentPage - 1) *
        rowsPerPage;

      return filteredCustomers.slice(
        start,
        start + rowsPerPage
      );

    }, [
      filteredCustomers,
      currentPage,
      rowsPerPage,
    ]);

  // -------------------------
  // Add Customer
  // -------------------------

  const handleAddCustomer = () => {

    setSelectedCustomer(null);

    setIsFormOpen(true);

  };

  // -------------------------
  // Edit
  // -------------------------

  const handleEditCustomer = (
    customer: CustomerResponse
  ) => {

    setSelectedCustomer(customer);

    setIsFormOpen(true);

  };

  // -------------------------
  // Close Form
  // -------------------------

  const handleCancel = () => {

    setSelectedCustomer(null);

    setIsFormOpen(false);

  };

  // -------------------------
  // Refresh after
  // create / update
  // -------------------------

  const handleSuccess =
    async () => {

      await fetchCustomers();

      setCurrentPage(1);

      setSelectedCustomer(null);

      setIsFormOpen(false);

    };

  // -------------------------
  // Deactivate
  // -------------------------

  const handleDeactivate =
    async () => {

      if (!customerToDeactivate)
        return;

      try {

        await customerApi.deleteCustomer(
          customerToDeactivate.customerId
        );

        toast.success(
          "Customer deactivated successfully."
        );

        await fetchCustomers();

        setCustomerToDeactivate(null);

      } catch (error) {

        console.error(error);

        toast.error(
          "Failed to deactivate customer."
        );

      }

    };


    const activeCustomers = customers.filter(
        (customer) => customer.status === "ACTIVE"
    ).length;

    const inactiveCustomers = customers.filter(
        (customer) => customer.status === "INACTIVE"
    ).length;

  return (
    <>
      <div className="space-y-6">

    <PageHeader
        title="Customers"
        description="View, add and track your customers."
        action={
          <Button onClick={handleAddCustomer}>
            <Plus size={18} />
            <span className="ml-2">Register Customer</span>
          </Button>
        }
    />

        {/* ---------- Start Card ---------- */}

            <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">

  <StatsCard
    title="Total Customers"
    value={totalCustomers}
    description="Registered customers"
    icon={<Users size={26} />}
  />

  <StatsCard
    title="Active Customers"
    value={activeCustomers}
    description="Currently active"
    icon={<UserCheck size={26} />}
  />

  <StatsCard
    title="Inactive Customers"
    value={inactiveCustomers}
    description="Currently inactive"
    icon={<UserX size={26} />}
  />

</div>

        {/* ---------- Search Card ---------- */}
        <SearchToolbar>

  <SearchToolbar.Left>

    <Input
      fullWidth
      leftIcon={<Search size={18} />}
      placeholder="Search customers by name, mobile or email..."
      value={search}
      onChange={(e) => setSearch(e.target.value)}
    />

  </SearchToolbar.Left>

  <SearchToolbar.Right>

    <div className="flex items-center gap-2">

      <span className="text-sm text-[var(--color-text-secondary)]">
        Rows
      </span>

      <select
        value={rowsPerPage}
        onChange={(e) => {
          setRowsPerPage(Number(e.target.value));
          setCurrentPage(1);
        }}
        className="
          h-10
          rounded-xl
          border border-[var(--color-border)]
          bg-white
          px-3
          text-sm
          outline-none
          transition-all
          focus:border-[var(--color-primary)]
          focus:ring-2
          focus:ring-[var(--color-primary)]/20
        "
      >
        <option value={10}>10</option>
        <option value={20}>20</option>
        <option value={50}>50</option>
      </select>

    </div>

  </SearchToolbar.Right>

</SearchToolbar>

<p className="mt-3 text-sm text-[var(--color-text-secondary)]">
  Showing{" "}
  <span className="font-semibold text-[var(--color-text)]">
    {totalCustomers}
  </span>{" "}
  customer{totalCustomers !== 1 ? "s" : ""}
</p>

          

        
      

        {/* ---------- Table ---------- */}

        <CustomerTable
          customers={paginatedCustomers}
          loading={loading}
          error={error}
          onEdit={handleEditCustomer}
          onDeactivate={
            setCustomerToDeactivate
          }
          currentPage={currentPage}
          totalPages={totalPages}
          onPrevious={() =>
            setCurrentPage((page) =>
              Math.max(1, page - 1)
            )
          }
          onNext={() =>
            setCurrentPage((page) =>
              Math.min(
                totalPages,
                page + 1
              )
            )
          }
        />

      </div>

      {/* ---------- Add / Edit Modal ---------- */}

      <CustomerForm
        open={isFormOpen}
        selectedCustomer={
          selectedCustomer
        }
        onSuccess={handleSuccess}
        onCancel={handleCancel}
      />

      {/* ---------- Deactivate Modal ---------- */}

      <Modal
        open={
          customerToDeactivate !== null
        }
        title="Deactivate Customer"
        onClose={() =>
          setCustomerToDeactivate(
            null
          )
        }
        footer={
          <>

            <Button
              variant="secondary"
              onClick={() =>
                setCustomerToDeactivate(
                  null
                )
              }
            >
              Cancel
            </Button>

            <Button
              variant="danger"
              onClick={
                handleDeactivate
              }
            >
              Deactivate
            </Button>

          </>
        }
      >

        <p>

          Are you sure you want to deactivate{" "}

          <strong>

            {
              customerToDeactivate?.fullName
            }

          </strong>

          ?

        </p>

        <p className="mt-3 text-sm text-[var(--color-text-muted)]">

          The customer will become inactive and
          will no longer appear in active operations.

        </p>

      </Modal>

    </>
  );

};

export default CustomerPage;