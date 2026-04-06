<script setup>
import { ref } from "vue"

const emit = defineEmits(["create", "close"])

// Colors for checkboxes
const colors = [
  "White","Brown","Black","Grey","Yellow",
  "Green","Orange","Blue","Purple","Pink","Red"
]

// Tags for dropdown
const tags = [
  "Top", 
  "Dress", 
  "Skirt", 
  "Pants", 
  "Accessory"
];

// Add selectedTag to form data
const item = ref({
  name: "",
  price: null,
  colors: [],
  tag: ""  // store selected tag
});

// Submit handler NOT DONE YET 
const submitForm = () => {
  emit("create", { ...item.value })

  // Reset form
  item.value = {
    name: "",
    price: null,
    colors: []
  }
}
</script>

<template>
  <div class="modal">
    <div class="modal-box">
      
      <div class="modal-header">
        <h2>Create Item</h2>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        
        <div class="image-box">
          <p>Upload Image</p>
        </div>

        <form class="form" @submit.prevent="submitForm">
          
          <label>Name:</label>
          <input v-model="item.name" placeholder="Item Name" required />

          <label>Tag:</label>
            <select v-model="item.tag" required>
                <option value="" disabled>Select a tag</option>
                <option v-for="tag in tags" :key="tag" :value="tag">{{ tag }}</option>
            </select>

          <label>Price:</label>
          <input
            type="number"
            step="0.01"
            min="0"
            v-model.number="item.price"
            placeholder="0.00"
            required
          />

          <label>Quantity:</label>
          <input type="number" min="1" placeholder="1" />

          <label>Colors:</label>
            <div class="color-grid">
            <label
                v-for="color in colors"
                :key="color"
                class="container"
            >
            {{ color }}
            <input type="checkbox" :value="color" v-model="item.colors" />
             <span class="checkmark"></span>
            </label>
            </div>

          <button class="submit-btn" type="submit">Done</button>
        </form>
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

/* Main box */
.modal-box {
  width: 700px;
  background: #f5f5f5;
  border: 3px solid #2f2fbf;
  box-shadow: 8px 8px 0px rgba(0,0,0,0.5);
}

/* Header */
.modal-header {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #cbb7d9;
  border-bottom: 2px solid #2f2fbf;
  position: relative;
  padding: 8px;
}

.modal-header h2 {
  margin: 0;
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

/* Body layout */
.modal-body {
  display: flex;
  gap: 30px;
  padding: 20px;
}

/* Image box */
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
}

/* Form */
.form {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 10px;
  align-items: center;
  flex: 1;
}

.form label,
.container {
  font-size: 16px;
}

/* Inputs */
.form input {
  border-radius: 10px;
  border: none;
  padding: 10px;
  box-shadow: inset 2px 2px 4px rgba(0,0,0,0.2);
}

.form select {
  border-radius: 10px;
  border: none;
  padding: 10px;
  box-shadow: inset 2px 2px 4px rgba(0,0,0,0.2);
}

/* Color grid */
.color-grid {
  grid-column: span 2;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

/* Button */
.submit-btn {
  grid-column: span 2;
  margin-top: 15px;
  padding: 8px;
  border-radius: 12px;
  border: 1px solid #888;
  background: linear-gradient(to bottom, #f7d1e0, #e6a9c2);
  cursor: pointer;
  font-weight: bold;
}
</style>