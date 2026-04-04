<script setup>
import { ref, computed } from "vue";
const imgUrl = new URL("./assets/images/blouse.jpg", import.meta.url).href;

const sizes = ["XS", "S", "M", "L", "XL"];
const colors = [
  "White",
  "Brown",
  "Black",
  "Grey",
  "Yellow",
  "Green",
  "Orange",
  "Blue",
];

const filters = ref({
  outOfStock: false,
  lowStock: false,
  sizes: ["XS"], // XS checked by default (as in screenshot)
  colors: ["White", "Blue"], // White & Blue checked by default
});

const products = ref([
  {
    id: 1,
    name: "Flowy Blouse",
    image: "/images/blouse.jpg",
    sizes: ["XS", "S"],
    color: "White",
  },
  {
    id: 2,
    name: "Tailored Shirt",
    image: "/images/shirt.jpg",
    sizes: ["XS", "M", "L"],
    color: "Blue",
  },
  {
    id: 3,
    name: "V-Neck sweater",
    image: "/images/sweater.jpg",
    sizes: ["XS", "S", "M"],
    color: "White",
  },
]);

const filteredProducts = computed(() => {
  return products.value.filter((product) => {
    const sizeMatch =
      filters.value.sizes.length === 0 ||
      product.sizes.some((s) => filters.value.sizes.includes(s));
    const colorMatch =
      filters.value.colors.length === 0 ||
      filters.value.colors.includes(product.color);
    return sizeMatch && colorMatch;
  });
});
</script>

<template>
  <div class="catalog">
    <!-- Left Sidebar -->

    <aside class="filters">
      <h3>Filter By:</h3>

      <!-- Stock filters -->
      <div class="filter-group">
        <h4>Stock</h4>
        <label
          ><input type="checkbox" v-model="filters.outOfStock" /> Show out
          <br />of stock</label
        >
        <label
          ><input type="checkbox" v-model="filters.lowStock" /> Show low
          <br />on stock</label
        >
      </div>

      <!-- Size filters -->
      <div class="filter-group">
        <h4>Size</h4>
        <label v-for="size in sizes" :key="size">
          <input type="checkbox" :value="size" v-model="filters.sizes" />
          {{ size }}
        </label>
      </div>

      <!-- Color filters -->
      <div class="filter-group">
        <h4>Color</h4>
        <label v-for="color in colors" :key="color">
          <input type="checkbox" :value="color" v-model="filters.colors" />
          {{ color }}
        </label>
      </div>
    </aside>

    <!-- Right Product Grid -->
    <main class="product-grid">
      <!-- Create New button card -->
      <div class="product-card create-card">
        <div class="plus-icon">+</div>
        <p>Create new</p>
      </div>

      <!-- Product cards (filtered) -->
      <div
        class="product-card"
        v-for="product in filteredProducts"
        :key="product.id"
      >
        <img :src="imgUrl" alt="test" style="width: 200px" />
        <p>{{ product.name }}</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
* {
  font-family: Inconsolata, monospace;
}
.catalog {
  display: flex;
  gap: 24px;
  background-color: white;
}

/* --- Sidebar --- */
.filters {
  background: linear-gradient(to right, #ffe5f9 49%, #ffffff 100%);
  min-width: 160px;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 16px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group h4 {
  text-decoration: underline;
  margin-bottom: 4px;
  font-size: 1.1rem;
}

.filter-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: monospace;
  font-size: 1rem;
}

/* --- Product Grid --- */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding: 24px;
  flex: 1;
  align-items: start;
}

.product-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  height: 320px;
}

.product-card p {
  font-size: 1rem;
  margin: 0;
}

.product-card img {
  width: 100%;
  height: 280px;
  object-fit: contain;
}

/* Create New card */
.create-card {
  background: #f7f7f7;
  border-radius: 16px;
  justify-content: center;
  min-height: 320px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.plus-icon {
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 50%;
  font-size: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
  cursor: pointer;
}

h3 {
  margin: 0;
  font-size: 1.2rem;
}
</style>
