<template>
  <div class="desktop">
    <div class="specific-item-view window">

      // Window Popup
      <div class="window-body">

        // left side of the window (image and stock)
        <div class="left">
          <div class="image">
            <img src="https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=400&q=80" alt="Flowy Blouse" />
          </div>
          <p class="stock">Stock left: {{ product.stock }} items</p>
        </div>

        // right side of the window (details and choices)
        <div class="right">

          <div class="row">
            <span class="lbl">Name:</span>
            <span class="val">{{ product.name }}</span>
          </div>

          <div class="row">
            <span class="lbl">Price:</span>
            <span class="val">${{ product.price.toFixed(2) }}</span>
          </div>

          <div class="row">
            <span class="lbl">Quantity:</span>
            <div class="qty-ctrl">
              <button @click="qty > 0 && qty--">+</button>
              <span>{{ qty }}</span>
              <button @click="qty < product.stock && qty++">-</button>
            </div>
          </div>

          <div class="colors-block">
            <span class="lbl">Colors:</span>
            <div class="color-grid">
              <label v-for="c in product.colors" :key="c" class="color-check">
                <input type="checkbox" v-model="selectedColors" :value="c" />
                {{ c }}
              </label>
            </div>
          </div>

        </div>
      </div>

      // button to add to cart
      <div class="button">
        <button class="cart-btn" @click="addToCart">
          {{ added ? '✓ Added to Cart!' : 'Add to Cart' }}
        </button>
      </div>

    </div>
  </div>
</template>

// dummy data
<script>
export default {
  name: 'ItemView',
  data() {
    return {
      qty: 0,
      selectedColors: [],
      added: false,
      product:  null
      
    }
  },

  // 1) called automatically when the component loads
  async mounted() {
    const res = await fetch('http://localhost:8080/api/products/1')
    this.product = await res.json()
    this.loading = false
  },

  methods: {
    // 2) sends cart data to your Spring Boot endpoint
    async addToCart() {
      await fetch('http://localhost:8080/api/cart', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          productId: this.product.id,
          quantity: this.qty,
          colors: this.selectedColors,
        })
      })
      this.added = true
      setTimeout(() => this.added = false, 2000)
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Courier+Prime:wght@400;700&display=swap');

* { box-sizing: border-box; margin: 0; padding: 0; }

.desktop {
  min-height: 100vh;
  background: #c0c0c0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  font-family: 'Courier Prime', 'Courier New', monospace;
}

.window {
  width: 620px;
  background: #fff;
  border: 3px solid #2a2abf;
  box-shadow: 6px 6px 0 #888;
}

.window-body {
  display: flex;
  gap: 32px;
  padding: 28px 28px 16px;
}

.left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.image {
  width: 200px;
  height: 220px;
  background: #f2f2f2;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stock {
  font-size: 13px;
  color: #333;
}


.right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lbl {
  font-size: 16px;
  font-weight: 700;
  color: #222;
  white-space: nowrap;
}

.val {
  background: #ececec;
  border-radius: 20px;
  padding: 5px 18px;
  font-size: 14px;
  color: #333;
}


.qty-ctrl {
  display: flex;
  align-items: center;
  background: #ececec;
  border-radius: 20px;
  overflow: hidden;
  padding: 0 4px;
  gap: 2px;
}
.qty-ctrl button {
  width: 28px;
  height: 30px;
  background: none;
  border: none;
  font-size: 16px;
  font-family: 'Courier Prime', monospace;
  cursor: pointer;
  color: #333;
}
.qty-ctrl button:hover { background: #ddd; border-radius: 50%; }
.qty-ctrl span {
  width: 32px;
  text-align: center;
  font-size: 14px;
}


.colors-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px 4px;
}

.color-check {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14px;
  color: #222;
  cursor: pointer;
}

.color-check input[type="checkbox"] {
  width: 15px;
  height: 15px;
  border: 2px solid #555;
  cursor: pointer;
  accent-color: #b39ddb;
  flex-shrink: 0;
}


.button {
  padding: 16px 28px 24px;
  display: flex;
  justify-content: center;
}

.cart-btn {
  background: #f8bbd0;
  border: 2px solid #888;
  border-radius: 24px;
  padding: 10px 48px;
  font-family: 'Courier Prime', monospace;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
  box-shadow: 2px 2px 0 #aaa;
}
.cart-btn:hover { background: #f48fb1; transform: translate(-1px,-1px); box-shadow: 3px 3px 0 #aaa; }
.cart-btn:active { transform: translate(1px,1px); box-shadow: 1px 1px 0 #aaa; }
</style>