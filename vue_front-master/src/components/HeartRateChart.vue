<template>
  <div ref="chart" style="width: 100%; height: 400px;"></div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'HeartRateChart',
  props: {
    heartRateData: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      chartInstance: null,
      restingHeartRate:55,
      dataCount:0,
    };
  },
  watch: {
    heartRateData: {
      handler(newData) {
        this.updateChart(newData);
      },
      deep: true, // Note: deep watching is usually not needed here since we're watching the entire object
      immediate: true // Execute the watcher immediately to handle initial data
    }
  },
  mounted() {
    this.chartInstance = echarts.init(this.$refs.chart);
    this.updateChart(this.heartRateData);
  },
  methods: {
    updateChart(data) {
      if(!data||!data.dataset){
        return;
      }
/*      const option = {
        xAxis: {
          type: 'category',
          data: data.dataset.map(item => item.time)
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: data.dataset.map(item => item.value),
          type: 'line'
        }]
      };*/
      this.dataCount=data.dataset.length;
      console.log("data:"+JSON.stringify(data));
      const option = {
        title: {
          text: echarts.format.addCommas(this.dataCount) + ' Data',
          left: 10
        },
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: false
            },
            saveAsImage: {
              pixelRatio: 2
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          bottom: 90
        },
        dataZoom: [
          {
            type: 'inside'
          },
          {
            type: 'slider'
          }
        ],
        xAxis: {
          data: data.dataset.map(item => item.time),
          silent: false,
          splitLine: {
            show: false
          },
          splitArea: {
            show: false
          }
        },
        yAxis: {
          splitArea: {
            show: false
          },
          axisLabel: {
            show: false // Hide data labels on the y-axis
          }
        },
        series: [
          {
            type: 'bar',
            data: data.dataset.map((hr,index) => ({
              value: hr.value,
              itemStyle: this.getItemStyle(hr.value),
              label: {
                show: true,
                rotate: 270, // Rotate 90 degrees to implement vertical display
                formatter: this.getLabelFormatter(hr.value,index),
              },
            })),
            // Set `large` for large data amount
            large: true
          },
        ]
      };

      this.chartInstance.setOption(option);
    },
    getItemStyle(hr) {
      const rhr = this.restingHeartRate;
      if (hr > rhr + 45) {
        return { color: 'red' };
      } else if (hr > rhr + 30) {
        return { color: 'orange' };
      } else {
        return { color: 'gray' };
      }
    },
    getLabelFormatter(hr,index) {
      /*if(index%Math.round(this.dataCount/20)!=0){
        return;
      }*/
      const rhr = this.restingHeartRate;
      let intensity = 0;
      let label = 'normal';
      if (hr > rhr + 45) {
        intensity = Math.min(1.0, (hr - (rhr + 30)) / 15);
        label = 'Accute Stress';
      } else if (hr > rhr + 30) {
        intensity = Math.min(1.0, (hr - (rhr + 30)) / 15);
        label = 'Critical Zone';
      }
      if(intensity>0){
        return `${label}\nIntensity: ${intensity.toFixed(2)}`;
      }
      return "";
    },
  },
  beforeDestroy() {
    if (this.chartInstance) {
      window.removeEventListener('resize', this.chartInstance.resize);
      this.chartInstance.dispose(); // Clean up ECharts instance
    }
  }
};
</script>

<style scoped>
/* Style can be changed according to need */

.audio-box{
  display: flex;
  align-items: center;
  justify-content: center;
}

.audio-item{
  margin-left: 16px;
}
</style>