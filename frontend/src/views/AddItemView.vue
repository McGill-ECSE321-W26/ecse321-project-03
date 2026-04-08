<script setup>
import { ref, onBeforeUnmount } from "vue";

const emit = defineEmits(["create", "close"]);

// Those are the limitations for the image upload
const MAX_FILES = 10;
const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
const ALLOWED_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

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
  tag: "",
  quantity: 1
});

/** @type {import('vue').Ref<{ file: File, url: string }[]>} */
const imageEntries = ref([]);
const uploadError = ref("");
const fileInputRef = ref(null);

// revoke all the previews when the component is unmounted
function revokeAllPreviews() {
  for (const e of imageEntries.value) {
    URL.revokeObjectURL(e.url);
  }
  imageEntries.value = [];
}

onBeforeUnmount(() => {
  revokeAllPreviews();
});

function openFilePicker() {
  uploadError.value = "";
  fileInputRef.value?.click();
}

// validate the file type and size
function validateFile(file) {
  if (!ALLOWED_TYPES.has(file.type)) {
    return "Only JPEG, PNG, and WebP are allowed.";
  }
  if (file.size > MAX_FILE_SIZE_BYTES) {
    return "Each file must be 5MB or smaller.";
  }
  return null;
}

// takes in the file input change
function onFileInputChange(event) {
  const input = event.target;
  const picked = input.files ? Array.from(input.files) : [];
  input.value = "";

  if (!picked.length) return;

  const room = MAX_FILES - imageEntries.value.length;
  if (room <= 0) {
    uploadError.value = `You can upload at most ${MAX_FILES} images.`;
    return;
  }

  const messages = [];
  let added = 0;

  for (const file of picked) {
    if (added >= room) {
      messages.push(`Only ${room} more image(s) could be added (max ${MAX_FILES} total).`);
      break;
    }
    const err = validateFile(file);
    if (err) {
      messages.push(`${file.name}: ${err}`);
      continue;
    }
    imageEntries.value.push({
      file,
      url: URL.createObjectURL(file)
    });
    added += 1;
  }

  uploadError.value = messages.length ? messages.join(" ") : "";
}

function removeImage(index) {
  const [removed] = imageEntries.value.splice(index, 1);
  if (removed) URL.revokeObjectURL(removed.url);
  uploadError.value = "";
}

const submitForm = () => {
  const files = imageEntries.value.map((e) => e.file);
  emit("create", { ...item.value }, files.length ? files : null);
};
</script>


<template>
  <div class="modal">
    <div class="modal-box">
      <div class="modal-header">
        <h2>Create Item</h2>
        <button type="button" class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
          <div class="form-inner">
            <div class="image-column">
              <input
                ref="fileInputRef"
                type="file"
                class="sr-only"
                accept="image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp"
                multiple
                @change="onFileInputChange"
              />
              <button
                type="button"
                class="image-box"
                :class="{ 'image-box--has-files': imageEntries.length }"
                @click="openFilePicker"
              >
                <template v-if="!imageEntries.length">
                  <span class="image-box-label">Upload Image</span>
                  <span class="image-box-hint">JPEG, PNG, WebP · up to {{ MAX_FILES }}</span>
                </template>
                <template v-else>
                  <span class="image-box-label">Add more</span>
                  <span class="image-box-hint">{{ imageEntries.length }} / {{ MAX_FILES }}</span>
                </template>
              </button>

              <p v-if="uploadError" class="upload-error" role="alert">{{ uploadError }}</p>

              <ul v-if="imageEntries.length" class="preview-list">
                <li v-for="(entry, index) in imageEntries" :key="entry.url" class="preview-item">
                  <img :src="entry.url" :alt="entry.file.name" class="preview-thumb" />
                  <button
                    type="button"
                    class="preview-remove"
                    :aria-label="`Remove ${entry.file.name}`"
                    @click.stop="removeImage(index)"
                  >
                    ✕
                  </button>
                </li>
              </ul>
            </div>

            <form class="form" @submit.prevent="submitForm">
              <label>Name:</label>
              <input v-model="item.name" placeholder="Item Name" required />

              <label>Tag:</label>
              <select v-model="item.tag" required>
                <option value="" disabled >Select a tag</option>
                <option v-for="tag in tags" :key="tag" :value="tag">{{ tag }}</option>
              </select>

              <label>Price:</label>
              <input type="number" step="0.01" min="0" v-model.number="item.price" placeholder="0.00" required />

              <label>Quantity:</label>
              <input type="number" min="1" placeholder="1" v-model.number="item.quantity" />

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
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

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
  align-items: flex-start;
}

.image-column {
  width: 200px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.image-box {
  width: 100%;
  min-height: 120px;
  padding: 12px;
  background: #ddd;
  border-radius: 20px;
  box-shadow: 4px 4px 6px rgba(0,0,0,0.3);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  border: 2px dashed #999;
  cursor: pointer;
  font-family: inherit;
  text-align: center;
  color: inherit;
}

.image-box:hover {
  background: #d0d0d0;
  border-color: #2f2fbf;
}

.image-box--has-files {
  min-height: 72px;
  border-style: solid;
  border-color: #2f2fbf;
}

.image-box-label {
  font-weight: bold;
}

.image-box-hint {
  font-size: 12px;
  color: #444;
  line-height: 1.3;
}

.upload-error {
  margin: 0;
  font-size: 12px;
  color: #a30;
  line-height: 1.35;
}

.preview-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 280px;
  overflow-y: auto;
}

.preview-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}

.preview-thumb {
  display: block;
  width: 100%;
  height: 88px;
  object-fit: cover;
  background: #eee;
}

.preview-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 1px solid #333;
  background: rgba(255,255,255,0.92);
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.preview-remove:hover {
  background: #fff;
}

.form {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 10px;
  align-items: center;
  flex: 1;
  min-width: 0;
}

/* Gray placeholder text */
select:invalid {
  color: #727272; /* gray */
}

/* Black text when a valid option is selected */
select {
  color: #000; /* default for selected option */
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
