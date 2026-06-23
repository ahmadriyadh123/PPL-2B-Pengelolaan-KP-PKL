import React, { useState, useEffect } from 'react';
import 'antd/dist/antd.css';
import { CCardBody } from '@coreui/react';
import { Tabs, Spin } from 'antd';
import axios from 'axios';
import { useParams, useHistory } from 'react-router-dom';
import TabKriteriaForm from './tabKriteriaForm';

const { TabPane } = Tabs;

// FIX BUG-025: Hardcoded Values dipindah ke variabel konfigurasi
const TAB_CONFIG = [
  { key: '0', name: 'ETS Teori' },
  { key: '1', name: 'ETS Praktek' },
  { key: '2', name: 'EAS Teori' },
  { key: '3', name: 'EAS Praktek' },
  { key: '4', name: 'Lain-lain Teori' },
  { key: '5', name: 'Lain-lain Praktek' },
];

const PembobotanKriteriaMataKuliah = () => {
  const { id } = useParams();
  const history = useHistory();
  
  const [activeTab, setActiveTab] = useState('0');
  const [isSpinner, setIsSpinner] = useState(true);
  const [matkul, setMatkul] = useState({});
  const [formPenilaianGlobal, setFormPenilaianGlobal] = useState([]);
  const [allKriteriaData, setAllKriteriaData] = useState([]);

  axios.defaults.withCredentials = true;

  useEffect(() => {
    const fetchGlobalData = async () => {
      try {
        const mataKuliahResponse = await axios.get(
          `${process.env.REACT_APP_API_GATEWAY_URL}grade/courses/form`
        );
        const prodiId = mataKuliahResponse.data.data[0].prodi_id;
        
        const formPenilaianResponse = await axios.get(
          `${process.env.REACT_APP_API_GATEWAY_URL}grade/courses/criteria/evaluation-form/${prodiId}`
        );
        const dataKriteriaResponse = await axios.get(
          `${process.env.REACT_APP_API_GATEWAY_URL}grade/courses/component/criteria/form/${id}`
        );

        setMatkul(mataKuliahResponse.data.data.find((item) => item.id === parseInt(id)));
        setFormPenilaianGlobal(formPenilaianResponse.data.data);
        setAllKriteriaData(dataKriteriaResponse.data.data);
        
        setIsSpinner(false);
      } catch (error) {
        if (error.response) {
          const status = error.response.status;
          if (status >= 300 && status <= 399) history.push('/login');
          else if (status >= 400 && status <= 499) history.push('/404');
          else if (status >= 500 && status <= 599) history.push('/500');
        }
      }
    };

    fetchGlobalData();
  }, [id, history]);

  // Fungsi dinamis untuk mencari data kriteria berdasarkan nama tab
  const getKriteriaDataByTabName = (name) => {
    const searchData = allKriteriaData.find((item) => item.name === name);
    return searchData ? searchData : {};
  };

  if (isSpinner) {
    return <Spin size="large" style={{ display: 'flex', justifyContent: 'center', marginTop: '50px' }} />;
  }

  return (
    <CCardBody style={{ paddingLeft: '20px' }}>
      <Tabs type="card" activeKey={activeTab} onChange={(key) => setActiveTab(key)}>
        
        {/* FIX BUG-028: Refactor menggunakan map untuk mengurangi perulangan kode */}
        {TAB_CONFIG.map((tab) => (
          <TabPane tab={tab.name} key={tab.key}>
            <TabKriteriaForm
              tabName={tab.name}
              matkul={matkul}
              formPenilaianOptions={formPenilaianGlobal}
              initialData={getKriteriaDataByTabName(tab.name)}
              idParam={id}
            />
          </TabPane>
        ))}
        
      </Tabs>
    </CCardBody>
  );
};

export default PembobotanKriteriaMataKuliah;