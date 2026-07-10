const API_BASE = "http://localhost:8080/api";

const mealSessionSelect =
    document.getElementById("mealSession");

const refreshBtn =
    document.getElementById("refreshBtn");

const tableBody =
    document.getElementById("collectionTableBody");

let currentMenuId = null;

refreshBtn.addEventListener("click", loadDashboard);
mealSessionSelect.addEventListener("change", loadDashboard);

window.onload = () => {
    loadDashboard();
};

async function loadDashboard() {

    const mealSession = mealSessionSelect.value;

    try {

        const response = await fetch(
            `${API_BASE}/dashboard?mealSession=${mealSession}`
        );

        const result = await response.json();

        if (!response.ok || !result.success) {
            alert(result.message);
            return;
        }

        currentMenuId = result.data.menuId;

        renderCollectionTable(result.data.collectionQueue);

    }
    catch (error) {

        console.error(error);

        alert("Unable to load dashboard.");

    }

}

function renderCollectionTable(customers) {

    tableBody.innerHTML = "";

    customers.forEach(customer => {

        const row = document.createElement("tr");

        row.dataset.customerId = customer.customerId;
        row.dataset.mealResponseId = customer.mealResponseId;

        row.innerHTML = `

            <td>${customer.customerName}</td>

            <td>${customer.responseStatus}</td>

            <td>${customer.mealOption}</td>

            <td>${customer.extraRotiCount}</td>

            <td>

                <select class="mealOption">

                    <option value="HALF"
                        ${customer.mealOption === "HALF" ? "selected" : ""}>
                        HALF
                    </option>

                    <option value="FULL"
                        ${customer.mealOption === "FULL" ? "selected" : ""}>
                        FULL
                    </option>

                </select>

            </td>

            <td>

                <input
                    class="extraRoti"
                    type="number"
                    min="0"
                    value="${customer.extraRotiCount}">

            </td>

            <td>

                ${customer.collected ? "✅" : "❌"}

            </td>

            <td>

                <button
                    class="${customer.collected ? "collected-btn" : "collect-btn"}"
                    ${customer.collected ? "disabled" : ""}
                    onclick="collectMeal(this)">

                    ${customer.collected ? "Collected" : "Collect"}

                </button>

            </td>

        `;

        tableBody.appendChild(row);

    });

}

async function collectMeal(button) {

    const row = button.closest("tr");

    const customerId =
        Number(row.dataset.customerId);

    const mealResponseId =
        Number(row.dataset.mealResponseId);

    const mealOption =
        row.querySelector(".mealOption").value;

    const extraRotiCount =
        Number(
            row.querySelector(".extraRoti").value
        );

    const request = {

        customerId: customerId,
        menuId: currentMenuId,
        mealResponseId: mealResponseId,
        mealOption: mealOption,
        extraRotiCount: extraRotiCount

    };

    try {

        const response = await fetch(
            `${API_BASE}/meal-records`,
            {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(request)

            }
        );

        const result = await response.json();

        if (!response.ok || !result.success) {

            alert(result.message);
            return;

        }

        alert(result.message);

        await loadDashboard();

    }
    catch (error) {

        console.error(error);

        alert("Meal collection failed.");

    }

}