/* Vue file (JavaScript + HTML + CSS) for the edit item modal. 
This is a child of the SpecificItemView.vue for a Manager
Author: Maria Klinski */

<script setup>
import { ref, watch } from 'vue'

// What the parent passes in
const props = defineProps({
  visible: Boolean,  // show or hide the modal depending on role
  product: Object    // the item being edited
})

// make the update or close
const emit = defineEmits(['close', 'update'])

//editable fields
const name      = ref('')
const price     = ref('')
const tag       = ref('')
const imageFile = ref(null)
const preview   = ref(null)

// Pre-fill when the modal opens
watch(() => props.visible, (val) => {
  if (val && props.product) {
    name.value      = props.product.name  || ''
    price.value     = props.product.price ?? ''
    tag.value       = props.product.tag   || ''
    imageFile.value = null
    preview.value   = null
  }
})

// Show a preview of the image
function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  imageFile.value = file
  preview.value   = URL.createObjectURL(file)
}

// update
function submit() {
  if (!name.value.trim()) return alert('Name is required!')
  const parsedPrice = parseFloat(price.value)
  if (isNaN(parsedPrice) || parsedPrice <= 0) return alert('Enter a valid price greater than 0!')

  emit('update', {
    name:      name.value.trim(),
    price:     parsedPrice,
    tag:       tag.value.trim(),
    imageFile: imageFile.value  // null if unchanged
  })
  emit('close')
}
</script>

<template>
  <div v-show="visible" class="modal-overlay">
    <div class="modal-box">

      <div class="modal-header-bar">
        <h2>Edit Item</h2>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <div class="form">
        <label>Name:</label>
        <input type="text" v-model="name" placeholder="Item name" />

        <label>Price ($):</label>
        <input type="number" min="0.01" step="0.01" v-model="price" placeholder="0.00" />

        <label>Tag:</label>
        <input type="text" v-model="tag" placeholder="e.g. NEW, SALE" />

        <label>Replace image:</label>
        <input type="file" accept="image/*" class="file-input" @change="onFileChange" />

        <img
          v-if="preview || product?.imageUrls?.[0]"
          :src="preview || product.imageUrls[0]"
          class="image-preview"
          alt="Preview"
        />

        <div class="button-container">
          <button class="action-btn cancel-btn" @click="$emit('close')">Cancel</button>
          <button class="action-btn save-btn"   @click="submit">Save changes</button>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal-box {
  width: 400px;
  max-height: 70vh;
  background: #f5f5f5;
  border: 3px solid #2f2fbf;
  box-shadow: 8px 8px 0px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header-bar {
  background: #b39ddb;
  border-bottom: 3px solid #2f2fbf;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.modal-header-bar h2 {
  font-family: 'Inconsolata', monospace;
  font-size: 18px;
  font-weight: 700;
  color: #222;
}

.close-btn {
  position: absolute;
  right: 10px;
  top: 6px;
  border: 2px solid black;
  background: #eee;
  cursor: pointer;
  font-size: 16px;
  border-radius: 4px;
  padding: 0 6px;
}

.form {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  gap: 12px;
}

.form label {
  font-family: 'Inconsolata', monospace;
  font-weight: bold;
  margin-top: 6px;
}

.form input[type='text'],
.form input[type='number'] {
  border-radius: 20px;
  border: none;
  padding: 10px;
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.2);
  height: 40px;
  font-size: 16px;
  width: 100%;
  box-sizing: border-box;
  font-family: 'Inconsolata', monospace;
}

.file-input {
  font-family: 'Inconsolata', monospace;
  font-size: 14px;
}

.image-preview {
  width: 100%;
  max-height: 140px;
  object-fit: contain;
  border-radius: 10px;
  border: 1px solid #ddd;
  background: #fff;
}

.button-container {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}

.action-btn {
  border-radius: 24px;
  padding: 10px 32px;
  font-family: 'Inconsolata', monospace;
  font-size: 16px;
  cursor: pointer;
  border: 2px solid #888;
  box-shadow: 2px 2px 0 #aaa;
  transition: background 0.15s, transform 0.1s;
}
.action-btn:hover  { transform: translate(-1px, -1px); box-shadow: 3px 3px 0 #aaa; }
.action-btn:active { transform: translate(1px, 1px);   box-shadow: 1px 1px 0 #aaa; }

.cancel-btn { background: #e0e0e0; }
.cancel-btn:hover { background: #cfcfcf; }

.save-btn { background: #f8bbd0; }
.save-btn:hover { background: #f48fb1; }
</style>