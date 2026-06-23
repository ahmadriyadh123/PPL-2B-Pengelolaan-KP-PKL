import React, { useState, useEffect } from 'react';
import { Form, Row, Col, Select, Switch, InputNumber, Button, notification } from 'antd';
import { PlusOutlined, MinusCircleOutlined } from '@ant-design/icons';
import axios from 'axios';

const TabKriteriaForm = ({ tabName, matkul, formPenilaianOptions, initialData, idParam }) => {
  const [form] = Form.useForm();
  const [isCustomWeight, setIsCustomWeight] = useState(false);
  const [formData, setFormData] = useState([]);
  const [tahapOptions, setTahapOptions] = useState([]);
  const [aspekOptions, setAspekOptions] = useState([]);
  const [komponen, setKomponen] = useState({});
  const [formName, setFormName] = useState('');

  // Setup initial data
  useEffect(() => {
    if (initialData?.criteria_data) {
      const mappedData = initialData.criteria_data.map((item) => ({
        name_form: item.name_form,
        type_form: item.type_form,
        aspect_form_id: item.aspect_form_id,
        aspect_name: item.aspect_name,
        component_id: item.component_id,
        bobot_criteria: item.bobot_criteria,
        id: item.id,
      }));
      setFormData(mappedData);
    }
  }, [initialData]);

  // Fetch komponen untuk tab ini berdasarkan nama tab
  useEffect(() => {
    const fetchKomponen = async () => {
      try {
        const res = await axios.get(`${process.env.REACT_APP_API_GATEWAY_URL}grade/courses/component/course-form/${idParam}`);
        const searchData = res.data.data.find((item) => item.name === tabName);
        setKomponen(searchData || {});
      } catch (error) {
        console.log(error);
      }
    };
    fetchKomponen();
  }, [idParam, tabName]);

  const handleFormPenilaianChange = async (value, index) => {
    setFormName(value);
    const selectedForm = formPenilaianOptions.find((item) => item.form_name === value);
    
    const updatedFormData = [...formData];
    updatedFormData[index] = {
      ...updatedFormData[index],
      name_form: selectedForm?.form_name,
      component_id: initialData?.id,
    };
    setFormData(updatedFormData);

    try {
      const res = await axios.get(`${process.env.REACT_APP_API_GATEWAY_URL}grade/courses/criteria/evaluation-form/aspect/type`, {
        params: { formType: value, prodiId: matkul.prodi_id },
      });
      setTahapOptions(res.data.data);
    } catch (error) {
      console.log(error);
    }
  };

  const handleTahapChange = async (value, index) => {
    const selectedForm = tahapOptions.find((item) => item.name === value);
    const updatedFormData = [...formData];
    updatedFormData[index] = {
      ...updatedFormData[index],
      type_form: selectedForm?.name,
    };
    setFormData(updatedFormData);

    try {
      const res = await axios.get(`${process.env.REACT_APP_API_GATEWAY_URL}grade/courses/criteria/evaluation-form/aspect`, {
        params: { formType: value, formName: formName, prodiId: matkul.prodi_id },
      });
      setAspekOptions(res.data.data);
    } catch (error) {
      console.log(error);
    }
  };

  const handleAspekChange = (value, index) => {
    const selectedForm = aspekOptions.find((item) => item.name === value);
    const updatedFormData = [...formData];
    updatedFormData[index] = {
      ...updatedFormData[index],
      aspect_form_id: selectedForm?.id,
      aspect_name: value,
      bobot_criteria: isCustomWeight ? updatedFormData[index].bobot_criteria : 100,
      id: null,
    };
    setFormData(updatedFormData);
  };

  const handleBobotChange = (value, index) => {
    const updatedFormData = [...formData];
    updatedFormData[index] = {
      ...updatedFormData[index],
      bobot_criteria: value,
    };
    setFormData(updatedFormData);
  };

  const handleRemoveField = (index, remove) => {
    const updatedFormData = [...formData];
    updatedFormData.splice(index, 1);
    setFormData(updatedFormData);
    remove(index);
  };

  const handleSubmit = async () => {
    try {
      await axios.put(`${process.env.REACT_APP_API_GATEWAY_URL}grade/courses/component/criteria/update`, {
        id: komponen.id,
        name: komponen.name,
        criteria_data: formData,
        is_average: 1,
      });
      notification.success({ message: `Data bobot kriteria ${tabName} tersimpan` });
    } catch (error) {
      console.error(error);
      notification.error({ message: `Data bobot kriteria ${tabName} gagal tersimpan` });
    }
  };

  return (
    <Row>
      <Col span={24}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', marginBottom: '10px' }}>
          <h6>Kustomisasi Bobot</h6>
          {/* FIX BUG-027 */}
          <Switch 
            checked={isCustomWeight} 
            onChange={setIsCustomWeight} 
            aria-label={`Aktifkan kustomisasi bobot untuk ${tabName}`} 
          />
        </div>
      </Col>
      <Col span={24}>
        <Form form={form} name={`form_${tabName}`} onFinish={handleSubmit} autoComplete="off">
          <Form.List name="kriteria">
            {(fields, { add, remove }) => (
              <>
                {fields.map((field, index) => (
                  <div key={field.key} style={{ marginBottom: 16 }}>
                    <Row gutter={16} align="middle">
                      <Col span={22}>
                        {/* FIX BUG-027: Gunakan prop label bawaan Form.Item daripada tag <b> */}
                        <Form.Item label="Evaluasi Form Penilaian" labelCol={{span: 24}} required>
                          <Select
                            defaultValue={formData[index]?.name_form}
                            onChange={(value) => handleFormPenilaianChange(value, index)}
                          >
                            {formPenilaianOptions.map((item) => (
                              <Select.Option key={item.form_type} value={item.form_type}>
                                {item.form_type}
                              </Select.Option>
                            ))}
                          </Select>
                        </Form.Item>

                        <Form.Item label="Evaluasi Penilaian" labelCol={{span: 24}} required>
                          <Select
                            defaultValue={formData[index]?.type_form}
                            onChange={(value) => handleTahapChange(value, index)}
                          >
                            {tahapOptions.map((item) => (
                              <Select.Option key={item.name} value={item.name}>
                                {item.name}
                              </Select.Option>
                            ))}
                          </Select>
                        </Form.Item>

                        <Form.Item label="Aspek" labelCol={{span: 24}} required>
                          <Select
                            defaultValue={formData[index]?.aspect_name}
                            onChange={(value) => handleAspekChange(value, index)}
                          >
                            {aspekOptions.map((item) => (
                              <Select.Option key={item.name} value={item.name}>
                                {item.name}
                              </Select.Option>
                            ))}
                          </Select>
                        </Form.Item>

                        {isCustomWeight && (
                          <Form.Item label="Bobot" labelCol={{span: 24}} required>
                            <InputNumber
                              min={0}
                              max={100}
                              addonAfter="%"
                              style={{ width: '100%' }}
                              defaultValue={formData[index]?.bobot_criteria}
                              onChange={(value) => handleBobotChange(value, index)}
                            />
                          </Form.Item>
                        )}
                      </Col>
                      
                      <Col span={2} style={{ textAlign: 'center' }}>
                        {/* FIX BUG-027: Gunakan element button untuk interaksi click */}
                        <Button
                          type="text"
                          danger
                          icon={<MinusCircleOutlined />}
                          onClick={() => handleRemoveField(field.name, remove)}
                          aria-label="Hapus kriteria ini"
                        />
                      </Col>
                    </Row>
                  </div>
                ))}
                
                <Form.Item>
                  <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                    Tambah Kriteria Penilaian
                  </Button>
                </Form.Item>
              </>
            )}
          </Form.List>

          <Form.Item>
            <Button type="primary" htmlType="submit">
              Submit
            </Button>
          </Form.Item>
        </Form>
      </Col>
    </Row>
  );
};

export default TabKriteriaForm;