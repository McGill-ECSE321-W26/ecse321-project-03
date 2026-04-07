const API_URL = "http://localhost:8080/items";

//getAllItems
export async function getAllItems(accountId) {
  console.log("[GET ALL ITEMS] accountId:", accountId);

  const url = `${API_URL}?accountId=${accountId}`;
  console.log("[GET ALL ITEMS] Request URL:", url);

  const response = await fetch(url, {
    method: "GET",
  });

  console.log("[GET ALL ITEMS] Response status:", response.status);

  if (!response.ok) {
    const errorText = await response.text();
    console.error("[GET ALL ITEMS] Error response:", errorText);
    throw new Error("Failed to fetch items");
  }

  const data = await response.json();
  console.log("[GET ALL ITEMS] Data received:", data);

  return data;
}