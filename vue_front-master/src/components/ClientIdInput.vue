<template>
  <div>
    <el-button type="primary" @click="showDialog = true">Login</el-button>

    <el-dialog
        title="Enter ClientId"
        :visible.sync="showDialog"
        width="30%"
        @close="closeDialog"
    >
      <el-input
          v-model="clientId"
          placeholder="Please enter your ClientId"
          @keyup.enter.native="submitClientId"
      ></el-input>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">Cancel</el-button>
        <el-button type="primary" @click="submitClientId">Submit</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  props: {
    initialValue: {
      type: String,
      default: '23PRWM'
    },
    onLogin: {
      type: Function,
      required: true
    }
  },
  data() {
    return {
      showDialog: false,
      clientId: this.initialValue
    };
  },
  methods: {
    submitClientId() {
      this.onLogin(this.clientId);
      this.closeDialog();
      // this.clientId = this.initialValue;
    },
    closeDialog() {
      this.showDialog = false;
      // this.clientId = this.initialValue;
    }
  }
};
</script>

<style scoped>
</style>