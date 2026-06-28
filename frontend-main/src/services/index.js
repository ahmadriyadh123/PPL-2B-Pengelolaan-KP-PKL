import axios from "axios";
import React from "react";

const Get = (path)  =>  {
    const promise = new Promise((resolve, reject)=>{
        axios.get( `${process.env.REACT_APP_API_GATEWAY_URL}${path}`)
        .then((result) => {
                resolve(result.data);
        },(err) => {
            reject(err);
        })
    })
    return promise
} 

//sementara data get nama pembimbing
const getDataArtifak = () => Get('pembimbing');


const API = {
    getDataArtifak
}

export default API