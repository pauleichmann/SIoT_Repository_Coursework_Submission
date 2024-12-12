<!-- AudioPlayer.vue -->
<template>
  <div class="audio-box">
    <h3>{{title}}</h3>
    <audio class="audio-item" ref="audioElement" @loadeddata="onAudioLoaded" :src="audioSrc"  preload="auto" controls></audio>
    <!--    <div v-show="audioObj.showAnalysis">transcription {{transcription}}</div>-->
    <!--    <div v-show="audioObj.showAnalysis">emotion {{emotion}}</div>-->
    <!--    <div v-show="audioObj.showAnalysis">confidence {{confidence}}</div>-->
  </div>
</template>

<script>

export default {
  props: {
    qTitle: {
      type: String,
      required: true
    },
    audioUrl: {
      type: String,
      required: true
    },
    audioRec: {
      type: Object,
      required: true
    },
    /*    doAnalysis: {
          type: Function,
          required: true
        }*/
  },
  data() {
    return {
      title:this.qTitle,
      audioSrc: this.audioUrl, // use direct inputed audioUrl as src
      audioObj: this.audioRec,
      transcription:"analysis...",
      emotion:"analysis...",
      confidence:"analysis...",
      //analysisClick:this.doAnalysis
    };
  },
  watch: {
    audioRec: {
      handler(newData) {

      },
      deep: true, //  Note: Deep watching is usually not needed here since we're watching the entire object directly
      immediate: true //  Execute the watcher immediately to handle initial data
    },
  },
  mounted() {
    this.preloadAudio();
  },
  methods: {

    preloadAudio() {
      const audioElement = this.$refs.audioElement;
      // src already bonded in data，use load() directlyto ensure loading or audio for website
      audioElement.load();
    },
    onAudioLoaded() {
      console.log(`Audio ${this.audioUrl} loading complete`);
      //
    },
  }
};
</script>

<style scoped>
/* style can be changed according to need */

.audio-box{
  display: flex;
  align-items: center;
  justify-content: center;
}

.audio-item{
  margin-left: 16px;
}
</style>