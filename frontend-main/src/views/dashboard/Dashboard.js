import React, {
  useEffect, useState, useRef
} from 'react'
import axios from 'axios'
import { useHistory } from 'react-router-dom';
import {
  CCard,
  CCardBody,
  CCardHeader,
} from '@coreui/react';
import Chart, {
  CommonSeriesSettings, Legend, SeriesTemplate, Animation, ArgumentAxis, Tick, Title, Tooltip, ValueAxis
} from 'devextreme-react/chart';
import { LoadingOutlined, TeamOutlined, BankOutlined, FileDoneOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { Spin, Table, Row, Col, Card, Statistic } from 'antd'
const antIcon = <LoadingOutlined style={{ fontSize: 40 }} spin />;

const Dashboard = () => {
  let history = useHistory();
  const [timeline, setTimeline] = useState([])
  const [date, setDate] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const cancelTokenSource = useRef(null);
  axios.defaults.withCredentials = true;

  const getData = (data) => {
    for (var i = 0; i < data.length; i++) {
      data[i].start_date = new Date(data[i].start_date);
      data[i].end_date = new Date(data[i].end_date);
    }
    return data;
  }

  const columns = [
    {
      title: 'No',
      dataIndex: 'no',
      width: '5%',
      align: "center",
      render: (value, item, index) => {
        return index + 1
      }
    },
    {
      title: 'Nama Kegiatan',
      dataIndex: 'name',
      width: '40%'
    },
    {
      title: 'Tanggal',
      dataIndex: 'tanggal',
      align: "center"
    }];

  useEffect(() => {
    const source = axios.CancelToken.source();
    cancelTokenSource.current = source;
    let isMounted = true;

    const getTimeline = async () => {
      await axios.get(`${process.env.REACT_APP_API_GATEWAY_URL}management-content/timeline`, { cancelToken: source.token })
        .then(function (response) {
          if (isMounted) {
            setTimeline(response.data.data)
            setIsLoading(false)
          }
        })
        .catch(function (error) {
          if (axios.isCancel(error)) return;
          if (error.toJSON().status >= 300 && error.toJSON().status <= 399) {
            history.push({
              pathname: "/login",
              state: {
                session: true,
              }
            });
          } else if (error.toJSON().status >= 400 && error.toJSON().status <= 499) {
            history.push("/404");
          } else if (error.toJSON().status >= 500 && error.toJSON().status <= 599) {
            history.push("/500");
          }
        });
    }

    const getDates = (tanggal) => {
      let date = new Date(tanggal)
      return `${date.getDate()} ${date.toLocaleDateString('id-EN', { month: "long" })} ${date.getFullYear()}`
    }

    const getDate = async () => {
      const formIds = [3, 11, 12, 5, 6, 7, 8, 9, 10];
      const formNames = [
        "Pengisian Prerequisite Perusahaan",
        "Pelaksanaan Kegiatan KP",
        "Pelaksanaan Kegiatan PKL",
        "Evaluasi Peserta KP",
        "Evaluasi 1 Peserta PKL",
        "Evaluasi 2 Peserta PKL",
        "Evaluasi 3 Peserta PKL",
        null,
        null,
      ];

      try {
        const requests = formIds.map(id =>
          axios.get(`${process.env.REACT_APP_API_GATEWAY_URL}management-content/form-submit-time/${id}`, { cancelToken: source.token })
        );
        const responses = await Promise.all(requests);

        if (!isMounted) return;

        const data = responses.map((response, index) => ({
          id: index + 1,
          name: formNames[index] || response.data.data.name,
          tanggal: getDates(response.data.data.start_date) + " - " + getDates(response.data.data.end_date),
        }));

        setDate(data);
        setIsLoading(false);
      } catch (error) {
        if (axios.isCancel(error)) return;
        if (error.toJSON().status >= 300 && error.toJSON().status <= 399) {
          history.push({
            pathname: "/login",
            state: {
              session: true,
            }
          });
        } else if (error.toJSON().status >= 400 && error.toJSON().status <= 499) {
          history.push("/404");
        } else if (error.toJSON().status >= 500 && error.toJSON().status <= 599) {
          history.push("/500");
        }
      }
    }

    if (localStorage.getItem("id_role") === "2") {
      getDate();
    } else {
      getTimeline();
    }

    return () => {
      isMounted = false;
      source.cancel('Component unmounted');
    };
  }, [history]);

  const customizeTooltip = (arg) => {
    var options = { year: 'numeric', month: 'long', day: 'numeric' };
    var start_date = new Date(arg.point.data.start_date);
    var end_date = new Date(arg.point.data.end_date);
    return {
      text: `<b>${arg.point.data.description}</b> <br> ${start_date.toLocaleDateString("en-GB", options)} - ${end_date.toLocaleDateString("en-GB", options)}`,
    };
  }

  return isLoading ? (<div style={{ display: 'flex', justifyContent: 'center', marginTop: '100px' }}><Spin indicator={antIcon} /></div>) : (
    <>
      <div className="mb-4">
        <h3 style={{ fontWeight: 700, color: '#333' }}>Dashboard Overview</h3>
        <p style={{ color: '#666' }}>Ringkasan aktivitas {localStorage.getItem("id_prodi") === "0" ? "Kerja Praktik" : "Praktik Kerja Lapangan"}</p>
      </div>

      <Row gutter={[16, 16]} className="mb-4">
        <Col xs={24} sm={12} md={6}>
          <Card hoverable style={{ borderRadius: '12px', background: 'linear-gradient(135deg, #1890ff 0%, #0050b3 100%)', color: 'white', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} bodyStyle={{ padding: '20px' }}>
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px', fontWeight: 600 }}>Total Mahasiswa</span>}
              value={142}
              valueStyle={{ color: 'white', fontSize: '32px', fontWeight: 'bold' }}
              prefix={<TeamOutlined style={{ opacity: 0.8 }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card hoverable style={{ borderRadius: '12px', background: 'linear-gradient(135deg, #52c41a 0%, #237804 100%)', color: 'white', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} bodyStyle={{ padding: '20px' }}>
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px', fontWeight: 600 }}>Perusahaan Mitra</span>}
              value={45}
              valueStyle={{ color: 'white', fontSize: '32px', fontWeight: 'bold' }}
              prefix={<BankOutlined style={{ opacity: 0.8 }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card hoverable style={{ borderRadius: '12px', background: 'linear-gradient(135deg, #faad14 0%, #ad6800 100%)', color: 'white', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} bodyStyle={{ padding: '20px' }}>
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px', fontWeight: 600 }}>Dokumen Masuk</span>}
              value={230}
              valueStyle={{ color: 'white', fontSize: '32px', fontWeight: 'bold' }}
              prefix={<FileDoneOutlined style={{ opacity: 0.8 }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card hoverable style={{ borderRadius: '12px', background: 'linear-gradient(135deg, #722ed1 0%, #391085 100%)', color: 'white', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} bodyStyle={{ padding: '20px' }}>
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.8)', fontSize: '14px', fontWeight: 600 }}>Telah Dievaluasi</span>}
              value={98}
              suffix="%"
              valueStyle={{ color: 'white', fontSize: '32px', fontWeight: 'bold' }}
              prefix={<CheckCircleOutlined style={{ opacity: 0.8 }} />}
            />
          </Card>
        </Col>
      </Row>

      {localStorage.getItem("id_role") === "2" ? (
        <CCard className="mb-4 shadow-sm" style={{ borderRadius: '12px', border: 'none' }}>
          <CCardHeader style={{ paddingLeft: "20px", textAlign: "center", backgroundColor: '#fff', borderBottom: '1px solid #f0f0f0', borderTopLeftRadius: '12px', borderTopRightRadius: '12px' }}>
            <h5 style={{ margin: 0, fontWeight: 600, color: '#1890ff' }}>Tabel Jadwal Penting Kegiatan</h5>
          </CCardHeader>
          <CCardBody style={{ padding: "24px" }}>
            <Table columns={columns} dataSource={date} rowKey="id" pagination={false} bordered scroll={{ x: "max-content" }} />
          </CCardBody>
        </CCard>
      ) : (
        <CCard className="shadow-sm" style={{ borderRadius: '12px', overflow: 'hidden', border: 'none' }}>
          <CCardBody style={{ padding: 24, minHeight: 400 }}>
            <h5 style={{ fontWeight: 600, marginBottom: '24px', color: '#333' }}>Timeline Kegiatan</h5>
            <div style={{ overflowX: "scroll" }}>
              <Chart id="chart" dataSource={(getData(timeline).sort((a, b) => a.start_date < b.start_date ? 1 : -1))} barGroupPadding={0.4} rotated={true}>
                <ArgumentAxis>
                  <Tick visible={false} />
                </ArgumentAxis>
                <ValueAxis />
                <CommonSeriesSettings
                  type="rangeBar"
                  argumentField="name"
                  rangeValue1Field="start_date"
                  rangeValue2Field="end_date"
                  barOverlapGroup="description"
                />
                <Legend visible={false} />
                <Tooltip enabled={true} customizeTooltip={customizeTooltip} />
                <SeriesTemplate nameField="name" />
                <Animation enabled={true} />
              </Chart>
            </div>
          </CCardBody>
        </CCard>
      )}
    </>
  )
}

export default Dashboard
