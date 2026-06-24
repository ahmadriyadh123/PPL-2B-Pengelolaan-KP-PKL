import React, { useState, useEffect } from 'react'
import axios from 'axios'
import 'antd/dist/reset.css'
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
    getDataForm()
  }, [history])

  const getDataForm = async () => {
    axios.defaults.withCredentials = true
    await axios
      .get(`${process.env.REACT_APP_API_GATEWAY_URL}grade/evaluation/`)
      .then(function (response) {
        setData(response.data.data)
        setIsLoading(false)
      })
      .catch(function (error) {
        const status = error.response ? error.response.status : (error.toJSON && typeof error.toJSON === 'function' ? error.toJSON().status : null);
        if (status) {
          if (status >= 300 && status <= 399) {
            history.push({
              pathname: '/login',
              state: {
                session: true,
              },
            })
          } else if (status >= 400 && status <= 499) {
            history.push('/404')
          } else if (status >= 500 && status <= 599) {
            history.push('/500')
          }
        } else {
          console.error(error)
          setIsLoading(false)
        }
      })
  }
  
  useEffect(() => {
    console.log("isi data =>>", data);
  },[data])

  const renderRows = () => {
    const rows = [];
    if (!Array.isArray(data)) return rows;
  
    data.forEach((row, rowIndex) => {
      const evaluations = row.evaluations || [];
      const itemsCount = evaluations.length;
  
      if (itemsCount === 0) {
        rows.push({
          key: `${rowIndex}-0`,
          no: rowIndex + 1,
          id: row.id,
          name: row.name,
          nim: row.nim,
          numEvaluation: '-',
          idNumEvaluation: null,
          rowSpan: 1
        });
      } else {
        evaluations.forEach((evaluation, itemIndex) => {
          if (itemIndex === 0) {
            rows.push({
              key: `${rowIndex}-${itemIndex}`,
              no: rowIndex + 1,
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
      }
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
        record.idNumEvaluation ? (
          <a href={`nilaiIndustri/${record.idNumEvaluation}`}>{record.numEvaluation}</a>
        ) : (
          record.numEvaluation
        )
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
