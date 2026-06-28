import React, { useState, useEffect } from 'react'
import axios from 'axios'
import 'antd/dist/antd.css'
import 'src/scss/_custom.scss'
import { CCard, CCardBody } from '@coreui/react';
import { LoadingOutlined } from '@ant-design/icons'
import { Button, Col, Row, Table, Spin, Form, Modal, Input, notification } from 'antd'
import { useHistory as history } from 'react-router-dom'

const antIcon = <LoadingOutlined style={{ fontSize: 40 }} spin />
const ListNilaiIndustri = () => {
  const [isLoading, setIsLoading] = useState(true)
  const [loadings, setLoadings] = useState([])
  const [data, setData] = useState([])

  axios.defaults.withCredentials = true

  // initial
  useEffect(() => {
    const source = axios.CancelToken.source();
    let isMounted = true;

    const getDataForm = async () => {
      axios.defaults.withCredentials = true
      await axios
        .get(`${process.env.REACT_APP_API_GATEWAY_URL}grade/evaluation/`, { cancelToken: source.token })
        .then(function (response) {
          if (isMounted) {
            setData(response.data.data)
            setIsLoading(false)
          }
        })
        .catch(function (error) {
          if (axios.isCancel(error)) return;
          if (error.toJSON().status >= 300 && error.toJSON().status <= 399) {
            history.push({
              pathname: '/login',
              state: {
                session: true,
              },
            })
          } else if (error.toJSON().status >= 400 && error.toJSON().status <= 499) {
            history.push('/404')
          } else if (error.toJSON().status >= 500 && error.toJSON().status <= 599) {
            history.push('/500')
          }
        })
    }

    getDataForm()

    return () => {
      isMounted = false;
      source.cancel("Component unmounted");
    }
  }, [history])
  
  useEffect(() => {
    console.log("isi data =>>", data);
  },[data])

  const renderRows = () => {
    const rows = [];
  
    data.forEach((row, rowIndex) => {
      const itemsCount = row.evaluations.length;
  
      row.evaluations.forEach((evaluation, itemIndex) => {
        if (itemIndex === 0) {
          rows.push({
            key: `${rowIndex}-${itemIndex}`,
            no: rowIndex+1,
            id: row.id,
            name: row.name,
            nim: row.nim,
            numEvaluation: `Evaluasi ${evaluation.numEvaluation}`,
            idNumEvaluation: evaluation.id,
            rowSpan: itemsCount
          });
        } else {
          rows.push({
            key: `${rowIndex}-${itemIndex}`,
            numEvaluation: `Evaluasi ${evaluation.numEvaluation}`,
            idNumEvaluation: evaluation.id,
            rowSpan: 0
          });
        }
      });
    });
    return rows;
  };

  const columns = [
    {
      title: 'No',
      dataIndex: 'no',
      key: 'no',
      width: '5%',
      align: 'center',
      render: (text, record) => ({
        children: text,
        props: {
          rowSpan: record.rowSpan
        }
      })
    },
    {
      title: 'NIM',
      dataIndex: 'nim',
      key: 'nim',
      align: 'center',
      render: (text, record) => ({
        children: text,
        props: {
          rowSpan: record.rowSpan
        }
      })
    },
    {
      title: 'Nama',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => ({
        children: text,
        props: {
          rowSpan: record.rowSpan
        }
      })
    },
    {
      title: 'Jenis Evaluasi',
      dataIndex: 'numEvaluation',
      key: 'numEvaluation',
      align: 'center',
      render: (text, record) => (
        <a href={`nilaiIndustri/${record.idNumEvaluation}`}>{record.numEvaluation}</a>
      ),
    }
  ];

  return isLoading ? (
    <Spin indicator={antIcon} />
  ) : (
    <CCard>
      <CCardBody>
        <Table
          dataSource={renderRows()}
          columns={columns}
          bordered
          pagination={{
            pageSize: 20
          }}
        />
      </CCardBody>
    </CCard>
  );
};

export default ListNilaiIndustri;
