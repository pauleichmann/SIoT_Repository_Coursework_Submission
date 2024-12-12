<template>
  <div class="emotional-evaluation">
    <!-- Upper section -->
    <div class="upper-section">
      <el-row :gutter="20">
        <!-- Left: Date selection and heart rate chart -->
        <el-col :span="8">
          <!-- Control buttons area -->
          <el-row style="margin-bottom: 20px;height: fit-content">
            <el-col :span="6">
              <el-date-picker
                  v-model="queryDate"
                  @change="handleDateChange"
                  format="yyyy-MM-dd"
                  placeholder="Select date"
                  type="date"
                  value-format="yyyy-MM-dd"
                  style="width: 100%"
              />
            </el-col>
            <el-col :span="8" :offset="1">
              <client-id-input
                  v-show="!isLogin"
                  :initial-value="'23PRWM'"
                  :onLogin="login"
                  style="width: 100%"
              />
            </el-col>
            <el-col :span="4" :offset="1">
              <el-button
                  type="primary"
                  @click="submitForm"
                  style="width: 100%"
              >
                Load Data
              </el-button>
            </el-col>
            <el-col v-if="isLogin" :span="3" :offset="1">
              <el-button
                  @click="doLogout"
                  type="danger"
                  style="width: 100%"
              >
                Logout
              </el-button>
            </el-col>
          </el-row>

          <!-- Heart rate chart area -->
          <div>
            <h2>Heart Rate Display</h2>
            <heart-rate-chart
                :heart-rate-data="heartRateData['activities-heart-intraday']"

            />
          </div>
        </el-col>

        <!-- Middle: Q&A section -->
        <el-col class="middle-box" :span="8">
          <div class="toatal-prompt" v-html="totalPrompt" style="font-size: 18px"></div>
          <div v-if="currentQuestionIndex < questions.length" class="question-item">
            <div class="question-header">
              <h2 class="step-title">STEP {{ currentQuestionIndex + 1 }}</h2>
              <div class="question-text">{{ questions[currentQuestionIndex] }}</div>
            </div>

            <p class="hint" v-html="hints[currentQuestionIndex]"></p>

            <div class="question-controls">
              <button style="margin: 20px;font-size: 18px" @click="prevQuestionClick">prev</button>
              <button type="primary" @mousedown="handleMouseDown" @mouseleave="handleMouseUp" @mouseup="handleMouseUp">
                <img :src="btnMixImg" style="width: 60px"/>
              </button>
              <button style="margin: 20px;font-size: 18px" @click="nextQuestionClick">next</button>

              <div class="record-hint">{{ recordHint }}</div>
              <div v-show="analysisShow" class="analysis-button">
                <el-button type="primary" @click="displayAnalysis">analysis</el-button>
              </div>
            </div>
          </div>
        </el-col>

        <!-- Right: Recording list -->
        <el-col :span="8">
          <div style="">
            <div style="font-size: 1.5em;font-weight: bold">Record List</div>
            <AudioPlayer
                v-for="(recording, index) in recordings"
                :key="index"
                :audio-rec="recording"
                :audio-url="recording.url"
                :q-title="recording.qtitle"
            />
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Lower section: Analysis results -->
    <div class="lower-section" v-show="analysisResultsShow">
      <el-row :gutter="20" v-for="(record, index) in analysisRecords" style="border-style: dashed" :key="index">
        <el-col class="lower-step-title" :span="2">STEP {{ index + 1 }} </el-col>
        <!-- Voice analysis text -->
        <el-col :span="7">
          <div class="analysis-box">
            <h3>Voice Analysis Text</h3>
            <div class="analysis-content">
              {{ record.transcription ? record.transcription : 'No transcription availableNo transcription available'}}
            </div>
          </div>
        </el-col>

        <!-- Confidence level circle chart -->
        <el-col :span="7">
          <div class="analysis-box">
            <h3>Confidence Level</h3>
            <el-progress
                type="circle"
                :stroke-width="20"
                :percentage="currentAnalysisRecord ? currentAnalysisRecord.confidence : 0"
                :color="confidenceColor(record)"
            />
          </div>
        </el-col>

        <!-- Emotion description -->
        <el-col :span="7">
          <div class="analysis-box">
            <h3>Emotion Analysis</h3>
            <div class="emotion-content">
              {{ record.emotion ? record.emotion : 'No emotion analysis available' }}
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import AudioPlayer from "@/components/AudioPlayer.vue"
import HeartRateChart from '@/components/HeartRateChart.vue'
import ClientIdInput from '@/components/ClientIdInput.vue'
import imgMic from "@/assets/img.png"
import {getCodeChallenge, getLatestFitbitData, getLoginState, logout, uploadRecord,} from "../api/index.js"

export default {
  name: "HomeView",
  components: {
    AudioPlayer,
    HeartRateChart,
    ClientIdInput
  },
  data() {
    return {
      queryDate: this.formatDate(new Date()),
      heartRateData: {"activities-heart-intraday": {"dataset": null}},
      isLogin: false,
      btnMixImg: imgMic,
      totalPrompt: "Alright, let's take this step by step. I want to help you unpack what's going on today.",
      questions: [
        'Can you tell me what specific events or situations happened today that triggered this feeling of stress?',
        'How did you find yourself reacting to these situations as they happened?',
        'When you look back at these reactions, do you feel they reflect the way you would typically respond to stress, or does this feel heightened compared to usual?',
        'Were there moments when you felt able to regain control or redirect your thoughts, or did the stress persist throughout the day without much relief?',
        'Are there certain thoughts or fears that seem to be coming up more strongly than others?',
        'When thinking about the support or resources available to you, do you feel you have the tools to manage the stress, or does it feel like you\'re alone in facing it?',
        "If we could zoom out and look at the bigger picture, are there ongoing pressures that might be adding to the weight of today's stress?",
        "Lastly, when you think about going through tomorrow or the rest of the week, do you feel hopeful, or does it feel like this stress is here to stay?"
      ],
      hints: [
        'Try to think back to any particular moment when it felt the most intense and let me know if anything stands out.',
        'Were there any physical signs, like tension in your body, or particular thoughts that kept circling in your mind?',
        "I'm interested in whether this stress feels like a familiar pattern or something different from what you usually experience.",
        'This can help us understand if there are any breaks in the tension or whether it felt unmanageable.',
        "Sometimes our stress is tied to particular concerns, like feeling that we're not measuring up, worrying about the future, or feeling unsupported.",
        'This could include people around you, personal coping mechanisms, or even self-reassurance.',
        'This could be a buildup from past days or weeks, sometimes making an ordinary day feel much heavier.',
        "This might give us some insight into whether today's stress feels like a singular event or part of a bigger cycle."
      ],
      currentQuestionIndex: 0,
      recordHint: "Click button to start recording!",
      analysisShow: false,
      analysisResultsShow: false,
      recordings: [],
      analysisRecords:[],
      mediaRecorder: null,
      isRecording: false,
    }
  },
  computed: {
    currentAnalysisRecord() {
      if (!this.analysisRecords.length) return null
      return this.analysisRecords[this.currentQuestionIndex]
    }
  },
  async mounted() {
    let loginState=await getLoginState();
    this.isLogin=loginState=="1";
  },
  methods: {
    /**
     * Formats a Date object into YYYY-MM-DD string format
     * @param {Date} date - The date to format
     * @return {string} Formatted date string
     */
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0') // Add leading zero if needed
      const day = String(date.getDate()).padStart(2, '0') // Add leading zero if needed
      return `${year}-${month}-${day}`
    },

    /**
     * Shows the analysis results section
     */
    displayAnalysis(){
      this.analysisResultsShow = true;
    },

    /**
     * Handles user logout
     */
    doLogout(){
      this.isLogin = false;
      logout();
    },

    /**
     * Handles Fitbit OAuth login process
     * @param {string} inputClientId - Optional client ID override
     */
    async login(inputClientId){
      let codeChallenge = await getCodeChallenge();
      
      // Get base redirect URL from store
      let redirectUrl = this.$store.state.baseURL;
      let clientId = "23PRWM";
      if(inputClientId){
        clientId = inputClientId;
      }

      // Redirect to Fitbit OAuth page
      window.location.href = 'https://www.fitbit.com/oauth2/authorize?code_challenge_method=S256&code_challenge=' +
        codeChallenge + '&response_type=code&client_id=' + clientId + '&redirect_uri=' + 
        redirectUrl + '/api/fitbitCallBack&scope=activity+profile+settings+heartrate+electrocardiogram';
    },

    /**
     * Submits form to fetch Fitbit data for selected date
     */
    async submitForm() {
      try {
        // Check login status first
        let loginState = await getLoginState();
        this.isLogin = loginState == "1";
        if(!this.isLogin) {
          alert("Please login first!");
          return;
        }
      } catch (error) {
        console.error('loginError:' + error);
      }

      // Fetch data for selected date
      let params = {
        "queryDate": this.queryDate
      };
      let rec = await getLatestFitbitData(params);

      if(rec != undefined && rec != ''){
        this.heartRateData = rec;
      }
    },

    /**
     * Handles date change event from date picker
     * @param {string} value - Selected date value
     */
    async handleDateChange(value) {
      if (!value) {
        this.$message.warning('Please select a valid date');
        return;
      }

      try {
        await this.submitForm();
        this.$message.success('Data loaded successfully');
      } catch (error) {
        console.error('Failed to load data:', error);
        this.$message.error('Failed to load data');
      }
    },

    /**
     * Processes raw heart rate data into chart-friendly format
     * @param {Object} response - Raw heart rate data
     * @return {Array} Processed heart rate data array
     */
    processHeartRateData(response) {
      const result = [];

      if(!response){
        return result;
      }

      // Process each row of heart rate data
      response.rows.forEach(row => {
        const minnum = row.minnum;
        const heartData = JSON.parse(row.heartdata);
        
        if(!heartData){
          result.push({
            minute: minnum.toString(), // Convert to string to match output format
            rate: 0 // Convert to string to match output format
          });
          return result;
        }

        // Extract resting heart rate from data
        const activityHeart = heartData['activities-heart'][0];

        // Extract restingHeartRate (Note: This assumes the heartData object structure is fixed)
        const restingHeartRate = activityHeart.value.restingHeartRate;
        let srestingHeartRate = 0;
        if(restingHeartRate != undefined){
          srestingHeartRate = restingHeartRate + "";
        }

        // Add data to the result array
        result.push({
          minute: minnum.toString(), // Convert to string to match output format
          rate: srestingHeartRate // Convert to string to match output format
        });
      });

      return result;
    },

    /**
     * Handles mouse down event on recording button
     */
    handleMouseDown() {
      console.log('Button is pressed down');
      this.startRecording(this.currentQuestionIndex);
    },

    /**
     * Handles mouse up event on recording button
     */
    handleMouseUp() {
      if(this.isRecording){
        console.log('Button is released');
        this.stopRecording(this.currentQuestionIndex);
        if(this.currentQuestionIndex == this.questions.length-1){
          this.analysisShow = true;
        }
      }
    },

    /**
     * Navigates to previous question
     */
    prevQuestionClick(){
      if (this.currentQuestionIndex > 0){
        this.currentQuestionIndex--;
      }
    },

    /**
     * Navigates to next question if conditions are met
     */
    nextQuestionClick(){
      if ((this.currentQuestionIndex < this.questions.length - 1) && 
          (this.currentQuestionIndex < this.recordings.length - 1)) {
        this.currentQuestionIndex++;
      }
    },

    /**
     * Starts audio recording process
     */
    async startRecording() {
      this.recordHint = "Recording...";
      let that = this;
      try {
        // Request microphone access
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        this.mediaRecorder = new MediaRecorder(stream);

        let audioChunks = [];
        this.mediaRecorder.ondataavailable = event => {
          audioChunks.push(event.data);
        };

        // Handle recording stop event
        this.mediaRecorder.onstop = () => {
          // Create audio blob and prepare for upload
          const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
          const formData = new FormData();
          let qtitle = 'step ' + (this.currentQuestionIndex + 1);
          formData.append('qtitle', qtitle);
          formData.append('audioFile', audioBlob, "recording.wav");

          // Create recording object and analyze it
          let rec = {
            "index": that.currentQuestionIndex,
            "qtitle": qtitle,
            "url": URL.createObjectURL(audioBlob),
            "fileBlob": audioBlob
          };
          this.analysisRec(rec);

          // Update recordings array
          if(this.currentQuestionIndex > that.recordings.length-1){
            that.recordings.push(rec);
          } else {
            that.recordings[this.currentQuestionIndex] = rec;
          }

          // Advance to next question if available
          if (this.currentQuestionIndex < this.questions.length - 1) {
            this.currentQuestionIndex++;
          }

          // Upload recording to backend
          uploadRecord(formData)
            .then((response) => {
              console.log('Upload successful:', response.data);
            })
            .catch((error) => {
              console.error('Upload failed:', error);
            });
        };

        this.mediaRecorder.start();
        this.isRecording = true;
      } catch (error) {
        console.error('Error accessing media devices.', error);
      }
    },

    /**
     * Analyzes recorded audio
     * @param {Object} rec - Recording object containing audio data
     */
    async analysisRec(rec) {
      try {
        // Prepare and send audio file
        const formData = new FormData();
        formData.append('audioFile', rec.fileBlob, "recording.wav");
        const response = await fetch(this.$store.state.analysisUrl + "/upload", {
          method: "POST",
          body: formData,
        });

        if (!response.ok) {
          alert("Failed to upload the audio file.");
          return;
        }

        // Get task ID and start polling for results
        const data = await response.json();
        const taskId = data.task_id;
        if (taskId) {
          await this.pollResult(taskId, rec);
        }
      } catch (e) {
        console.log(e.message);
      }
    },

    /**
     * Polls for analysis results
     * @param {string} taskId - Task ID to poll for
     * @param {Object} rec - Recording object
     */
    async pollResult(taskId, rec) {
      const resultUrl = this.$store.state.analysisUrl + `/result/${taskId}`;
      let result;

      try {
        while (true) {
          const response = await fetch(resultUrl);
          result = await response.json();

          if (result.status === "completed") {
            // Process completed analysis
            let aRec = {
              index: rec.index,
              transcription: result.result["transcription"],
              emotion: result.result["emotion"],
              confidence: result.result["confidence"]
            };

            // Update analysis records array
            if(aRec.index <= (this.analysisRecords.length-1)){
              this.analysisRecords[aRec.index] = aRec;
            } else if (aRec.index <= 8){
              this.analysisRecords.push(aRec);
            }
            break;
          } else if (result.status === "error") {
            break;
          }

          // Poll every 2 seconds
          await new Promise(resolve => setTimeout(resolve, 2000));
        }
      } catch (error) {
        console.error("Error fetching result:", error);
      }
    },

    /**
     * Stops current recording
     */
    stopRecording() {
      if (this.mediaRecorder) {
        this.recordHint = "Click button to start recording!";
        this.isRecording = false;
        this.mediaRecorder.stop();
      }
    },

    /**
     * Determines confidence level color
     * @param {Object} record - Analysis record
     * @return {string} Color code based on confidence level
     */
    confidenceColor(record) {
      const confidence = record.confidence ? record.confidence : 0;
      if (confidence >= 80) return '#67C23A';  // Green for high confidence
      if (confidence >= 60) return '#E6A23C';  // Orange for medium confidence
      return '#F56C6C';  // Red for low confidence
    }
  }
};
</script>

<style lang="scss" scoped>
/* Main container for emotional evaluation */
.emotional-evaluation {
  display: flex;
  flex-direction: column;
  height: 100%;
  //padding: 5px;
  box-sizing: border-box;
}

/* Upper section containing charts and controls */
.upper-section {
  flex: 1;
  height: 50vh;
  margin-bottom: 20px;

  .el-row {
    height: 100%;
  }

  .el-col {
    height: 100%;
    overflow-y: hidden;
    display: flex;
    flex-direction: column;
  }
}

/* Lower section containing analysis results */
.lower-section {
  min-height: calc(100% - 50vh - 40px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background: white;
  border-radius: 8px;
  padding: 4px 0;
  box-sizing: border-box;

  .el-row {
    height: 100%;
    margin: 0 !important;
  }

  .el-col {
    height: 100%;
  }
}

/* Step title in the lower section */
.lower-step-title{
  height: 156px;
  line-height: 156px;
}

/* Box containing analysis results */
.analysis-box {
  max-height: 100%;
  padding: 10px;
  box-sizing: border-box;
  display: flex;
  justify-content: space-evenly;
  align-items: flex-start;

  h3 {
    margin: 0 0 10px 0;
    color: #303133;
    text-align: left;
    font-size: 16px;
    flex-shrink: 0;
    height: 24px;
    line-height: 24px;
  }

  .analysis-content {
    flex: 1;
    margin-left: 10px;
    padding: 10px;
    text-align: left;
    background: #f8f9fa;
    border-radius: 4px;
    font-size: 14px;
    overflow: auto;
    height: 100px;
  }

  .emotion-content {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: bold;
    color: #409EFF;
    height: 120px;
  }

  .el-progress {
    margin-top: 10px;
    height: calc(100% - 34px);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

/* Layout for heart rate, questions, and answered sections */
.heart-rate-section, .question-item, .answered-questions {
  background: white;
  border-radius: 8px;
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}
.middle-box{}
.question-item {
  display: flex;
  flex-direction: column;

  .question-header {
    flex-shrink: 0;
  }

  .hint {
    margin: 10px 0;
  }

  .question-controls {
    margin-top: 40px;
    flex-shrink: 0;

    .record-hint{
      margin-bottom: 30px;
    }

    .analysis-button{
      .el-button{
        width: 60%;
      }
    }
  }
}

.answered-questions {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-sizing: border-box;

  h4 {
    flex-shrink: 0;
    margin: 0 0 15px 0;
  }

  // add scrolling container
  & > div:not(h4) {  // content area except title
    flex: 1;
    overflow-y: auto;
    min-height: 0;  // Allow flex items to shrink
    padding-right: 10px;  // save space for scrolling bar
  }
}

.heart-rate-section {
  flex: 1;
  display: flex;
  flex-direction: column;

  h2 {
    margin: 0 0 15px 0;
  }

  & > div {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
}

/* adjust the layout for upper left controls */
.upper-section {
  .el-row:first-child {
    margin-bottom: 20px;
  }

  .el-date-picker {
    width: 100%;
  }

  .el-button {
    width: 100%;
  }
}
</style>
