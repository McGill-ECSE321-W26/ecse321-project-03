<template>
  <div class="catalog">
    <aside class="filters">
      <h3>Filter By:</h3>
      <div class="filter-group">
        <h4>Stock</h4>
        <label class="container">
          <input type="checkbox" v-model="filters.outOfStock" />
          <span class="checkmark"></span> Show out <br />of stock
        </label>
        <label class="container">
          <input type="checkbox" v-model="filters.lowStock" />
          <span class="checkmark"></span> Show low <br />on stock
        </label>
      </div>

      <div class="filter-group">
        <h4>Price</h4>
        <input type="range" min="0" :max="PRICE_LIMIT" step="5" v-model.number="maxPrice" />
        <span class="price-label">
          Up to: {{ maxPrice === PRICE_LIMIT ? "No limit" : "$" + maxPrice }}
        </span>
      </div>

      <div class="filter-group">
        <h4>Size</h4>
        <span class="toggle-all" @click="toggleAll('sizes')">
          {{ filters.sizes.length === sizes.length ? "Deselect all" : "All sizes" }}
        </span>
        <label class="container" v-for="size in sizes" :key="size">
          <input type="checkbox" :value="size" v-model="filters.sizes" />
          <span class="checkmark"></span> {{ size }}
        </label>
      </div>

      <div class="filter-group">
        <h4>Color</h4>
        <span class="toggle-all" @click="toggleAll('colors')">
          {{ filters.colors.length === colors.length ? "Deselect all" : "All colors" }}
        </span>
        <label class="container" v-for="color in colors" :key="color">
          <input type="checkbox" :value="color" v-model="filters.colors" />
          <span class="checkmark"></span> {{ color }}
        </label>
      </div>
    </aside>

    <main class="product-grid">
      <div
          class="product-card create-card"
          v-if="isManager"
          @click="showForm = true"
          style="cursor: pointer;"
      >
        <div class="plus-icon">+</div>
        <p>Create new</p>
      </div>

      <div
          class="product-card"
          v-for="product in filteredProducts"
          :key="product.id"
          @click="goToItem(product.id)"
          style="cursor: pointer;"
      >
        <img :src="product.image" :alt="product.name" />
        <p>{{ product.name }}</p>
        <p>${{ product.price?.toFixed(2) }}</p>
      </div>
    </main>
  </div>

  <AddItemView
      v-if="showForm"
      @close="showForm = false"
      @create="addItem"
  />
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import AddItemView from "./AddItemView.vue";
import { getSession } from "../session/storage";
import { useRouter } from "vue-router";
import { getAllItems } from "../api/item.js"; // Make sure to import this!

const imgUrl = new URL("./assets/images/blouse.jpg", import.meta.url).href;
const router = useRouter();

const sizes = ["XS", "S", "M", "L", "XL"];
const colors = ["White", "Brown", "Black", "Grey", "Yellow", "Green", "Orange", "Blue", "Purple", "Pink", "Red"];
const PRICE_LIMIT = 200; // Added this fallback so the slider works
const maxPrice = ref(PRICE_LIMIT);

const showForm = ref(false);
const isManager = ref(false);
const session = getSession();

if (session && session.accountType === "MANAGER") {
  isManager.value = true
};

const filters = ref({
  outOfStock: false,
  lowStock: false,
  sizes: [...sizes],
  colors: [...colors],
});

const products = ref([]);
const loading = ref(true);

function toggleAll(type) {
  if (type === "sizes") {
    filters.value.sizes = filters.value.sizes.length === sizes.length ? [] : [...sizes];
  } else if (type === "colors") {
    filters.value.colors = filters.value.colors.length === colors.length ? [] : [...colors];
  }
}

function goToItem(id) {
  router.push(`/item/${id}`);
}

const filteredProducts = computed(() => {
  return products.value.filter((product) => {
    if (!filters.value.outOfStock && product.totalStock === 0) return false;
    if (!filters.value.lowStock && product.hasLowStock) return false;

    const sizeMatch = filters.value.sizes.length === 0 || filters.value.sizes.some((s) => product.availableSizes.includes(s));
    const colorMatch = filters.value.colors.length === 0 || filters.value.colors.some((c) => product.availableColors.includes(c));
    const priceMatch = maxPrice.value === PRICE_LIMIT || product.price <= maxPrice.value;

    return sizeMatch && colorMatch && priceMatch;
  });
});

onMounted(async () => {
  try {
    const data = await getAllItems(session?.accountId || 1);
    products.value = data.map((item) => ({
      id: item.id,
      name: item.name,
      price: item.price,
      image: item.imageUrls && item.imageUrls.length > 0 ? `http://localhost:8080${item.imageUrls[0]}`: imgUrl,
      availableSizes: [...new Set(item.variants.map((v) => v.size))],
      availableColors: [...new Set(item.variants.map((v) => v.color))],
      totalStock: item.variants.reduce((sum, v) => sum + v.stockLevel, 0),
      hasLowStock: item.variants.some((v) => v.stockLevel > 0 && v.stockLevel <= 5),
    }));
  } catch (e) {
    console.error("Failed to load products:", e);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
* { font-family: Inconsolata, monospace; }
.catalog { display: flex; gap: 24px; background-color: white; }
.filters { background: linear-gradient(to right, #ffe5f9 49%, #ffffff 100%); min-width: 160px; display: flex; flex-direction: column; padding: 16px; padding-right: 30px; gap: 16px; }
.filter-group { display: flex; flex-direction: column; gap: 6px; }
.filter-group h4 { text-decoration: underline; margin-bottom: 4px; font-size: 1.1rem; }
.filter-group label { display: flex; align-items: center; gap: 8px; font-family: monospace; font-size: 1rem; }
.toggle-all { font-size: 0.9rem; text-decoration: underline; cursor: pointer; color: #555; }
.toggle-all:hover { color: #000; }
.price-label { font-size: 0.9rem; margin-top: 2px; }
input[type="range"] { width: 100%; accent-color: #1209ab; background-color: #ffffff; cursor: pointer; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; padding: 24px; flex: 1; align-items: start; }
.product-card { display: flex; flex-direction: column; align-items: center; gap: 8px; height: 320px; transition: transform 0.2s; }
.product-card:hover { transform: translateY(-5px); }
.product-card p { font-size: 1rem; margin: 0; }
.product-card img { width: 100%; height: 280px; object-fit: contain; }
.create-card { background: #f7f7f7; border-radius: 16px; justify-content: center; min-height: 320px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25); }
.plus-icon { width: 80px; height: 80px; background: white; border-radius: 50%; font-size: 50px; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25); }
h3 { margin: 0; font-size: 1.2rem; }
</style>