<script setup>
import { ref } from "vue";

const emit = defineEmits(["create", "close"]);

const colors = [
  "White","Brown","Black","Grey","Yellow",
  "Green","Orange","Blue","Purple","Pink","Red"
];

const sizes = ["XS", "S", "M", "L", "XL"];

const tags = ["Top", "Dress", "Skirt", "Pants", "Accessory"];

const item = ref({
  name: "",
  price: null,
  colors: [],
  sizes: [],
  tag: ""
});

const submitForm = () => {
  emit("create", { ...item.value });
  item.value = {
    name: "",
    price: null,
    colors: [],
    sizes: [],
    tag: ""
  };
};
</script>


<template>
  <div class="modal">
    <div class="modal-box">
      <div class="modal-header">
        <h2>Create Item</h2>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
          <div class="form-inner">
            <!-- Image box on the left -->
            <div class="image-box">
              <p>Upload Image</p>
            </div>

            <!-- Form on the right -->
            <form class="form" @submit.prevent="submitForm">
              <label>Name:</label>
              <input v-model="item.name" placeholder="Item Name" required />

              <label>Tag:</label>
              <select v-model="item.tag" required>
                <option value="" disabled>Select a tag</option>
                <option v-for="tag in tags" :key="tag" :value="tag">{{ tag }}</option>
              </select>

              <label>Price:</label>
              <input type="number" step="0.01" min="0" v-model.number="item.price" placeholder="0.00" required />

              <label>Quantity:</label>
              <input type="number" min="1" placeholder="1" />

              <label>Colors:</label>
              <div class="color-grid">
                <label v-for="color in colors" :key="color" class="container">
                  {{ color }}
                  <input type="checkbox" :value="color" v-model="item.colors" />
                  <span class="checkmark"></span>
                </label>
              </div>

              <label>Sizes:</label>
              <div class="color-grid">
                <label v-for="size in sizes" :key="size" class="container">
                  {{ size }}
                  <input type="checkbox" :value="size" v-model="item.sizes" />
                  <span class="checkmark"></span>
                </label>
              </div>

              <button class="submit-btn" type="submit">Done</button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-box {
  width: 700px;
  min-height: 600px;
  max-height: 90vh;
  background: #f5f5f5;
  border: 3px solid #2f2fbf;
  box-shadow: 8px 8px 0px rgba(0,0,0,0.5);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #cbb7d9;
  border-bottom: 2px solid #2f2fbf;
  position: relative;
  padding: 6px 8px;
  height: 40px;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  position: absolute;
  right: 10px;
  top: 5px;
  border: 2px solid black;
  background: #eee;
  cursor: pointer;
  font-size: 16px;
  border-radius: 4px;
  padding: 0 6px;
}

.modal-body {
  display: flex;
  flex: 1;
  padding: 20px;
  overflow: hidden; 
}


.form-container {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}

.form-inner {
  display: flex;
  gap: 30px;
}

.image-box {
  width: 180px;
  height: 220px;
  background: #ddd;
  border-radius: 20px;
  box-shadow: 4px 4px 6px rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  flex-shrink: 0;
}

.form {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 10px;
  align-items: center;
}

.form input,
.form select {
  border-radius: 20px;
  border: none;
  padding: 10px;
  box-shadow: inset 2px 2px 4px rgba(0,0,0,0.2);
  height: 40px;
  font-size: 16px;
  width: 100%;
  box-sizing: border-box;
  font-family: 'Inconsolata', monospace;
}

.color-grid {
  grid-column: span 2;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.color-grid .container {
  font-size: 16px;
  line-height: 1.2;
}

.submit-btn {
  grid-column: 1 / -1;
  justify-self: center;
  padding: 10px 30px;
  border-radius: 12px;
  border: 1px solid #888;
  background: linear-gradient(to bottom, #f7d1e0, #e6a9c2);
  cursor: pointer;
  font-weight: bold;
  text-align: center;
  margin-top: 15px;
}
</style>