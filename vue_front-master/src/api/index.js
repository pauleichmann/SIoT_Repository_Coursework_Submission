import {get,post} from '../utils/request.js';
import axios from "axios";
import store from '../store';

let baseURL =store.state.baseURL;// "https://mosaicpatientreporttools.com/"
console.log("baseURL:"+baseURL)
//let baseURL =  "http://localhost:8090"

export function getCodeChallenge(){
    return get(baseURL+"/api/getCodeChallenge", {});
}


export function getLoginState(){
    return get(baseURL+"/api/getLoginState", {});
}

export function uploadRecord(data){
    return axios.post(baseURL+'/api/uploadRecord', data, {
        headers: {
            'Content-Type':'multipart/form-data'
        }
    })
}

export function queryFitbitData(data){
    return get(baseURL+"/FitbitData/queryList", data);
}

export function getLatestFitbitData(data){
    return get(baseURL+"/FitbitData/getLatest", data);
}
export function logout(data){
    return get(baseURL+"/api/logout", data);
}

export function queryAudioDataList(){
    return get(baseURL+"/AudioData/queryList", {});
}

export function downloadRecord(fileName){
    return get(baseURL+"/AudioData/download", {"fileName":fileName});
    // return axios.get(baseURL+'/AudioData/download?fileName='+fileName, {
    //     responseType: 'blob', // specify response type as blob
    // });
}