<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  product: Object
})

const emit = defineEmits(['close', 'restock'])

const selectedColor = ref(null) 
const selectedSize = ref(null)
const quantity = ref(0)

// Reset selections when modal opens
watch(() => props.visible, (val) => {
  if (val && props.product) {
    selectedColor.value = null
    selectedSize.value = null
    quantity.value = 0
  }
})

const uniqueColors = computed(() => {
  if (!props.product) return []
  return [...new Set(props.product.variants.map(v => v.color))]
})

const sizesForColor = computed(() => {
  if (!props.product || !selectedColor.value) return []
  return props.product.variants
    .filter(v => v.color === selectedColor.value)
    .map(v => v.size)
})

const selectedVariant = computed(() => {
  if (!props.product || !selectedColor.value || !selectedSize.value) return null
  return props.product.variants.find(v => v.color === selectedColor.value && v.size === selectedSize.value)
})

const submit = () => {
  if (!selectedColor.value || !selectedSize.value) return alert('Please select both color and size!')
  if (quantity.value <= 0) return alert('Enter a quantity greater than 0!')

  emit('restock', [{
    id: selectedVariant.value.id,
    color: selectedVariant.value.color,
    size: selectedVariant.value.size,
    quantity: quantity.value
  }])
  emit('close')
}
</script>


<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-box">

      <div class="modal-header-bar">
        <h2>Restock Item</h2>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <div class="form">
        <label>Name:</label>
        <input :value="product?.name || ''" disabled />

        <label>Color:</label>
        <select v-model="selectedColor">
          <option value="" disabled>Select a color</option>
          <option v-for="color in uniqueColors" :key="color" :value="color">{{ color }}</option>
        </select>

        <label>Size:</label>
        <select v-model="selectedSize" :disabled="!selectedColor">
          <option value="" disabled>Select a size</option>
          <option v-for="size in sizesForColor" :key="size" :value="size">{{ size }}</option>
        </select>

        <label>Quantity:</label>
        <input type="number" min="1" v-model.number="quantity" />

        <div class="button-container">
          <button class="restock-btn" @click="submit">Done</button>
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
  background: rgba(0,0,0,0.4);
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
  box-shadow: 8px 8px 0px rgba(0,0,0,0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 12px;
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
  font-weight: bold;
  margin-top: 6px;
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

.button-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.restock-btn {
  background: #f8bbd0;
  border: 2px solid #888;
  border-radius: 24px;
  padding: 10px 48px;
  font-family: 'Inconsolata', monospace;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
  box-shadow: 2px 2px 0 #aaa;
}
.restock-btn:hover { background: #f48fb1; transform: translate(-1px,-1px); box-shadow: 3px 3px 0 #aaa; }
.restock-btn:active { transform: translate(1px,1px); box-shadow: 1px 1px 0 #aaa; }
</style>