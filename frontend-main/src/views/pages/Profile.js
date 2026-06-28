import React, { useState } from 'react'
import 'antd/dist/antd.css';
import { Modal, Form, Input, Button, notification, Upload, Avatar } from 'antd';
import { UploadOutlined, UserOutlined, LoadingOutlined } from '@ant-design/icons';
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CRow,
} from '@coreui/react'
import axios from 'axios';


const Profile = () => {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [form] = Form.useForm();
  const [loadings, setLoadings] = useState([]);
  const [imageUrl, setImageUrl] = useState(localStorage.getItem('profile_picture') || '');
  const [uploadLoading, setUploadLoading] = useState(false);
  axios.defaults.withCredentials = true;

  const getBase64 = (img, callback) => {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result));
    reader.readAsDataURL(img);
  };

  const beforeUpload = (file) => {
    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
    if (!isJpgOrPng) {
      notification.error({ message: 'Anda hanya bisa mengunggah file JPG/PNG!' });
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      notification.error({ message: 'Ukuran gambar harus lebih kecil dari 2MB!' });
    }
    return isJpgOrPng && isLt2M;
  };

  const handleUploadChange = (info) => {
    if (info.file.status === 'uploading') {
      setUploadLoading(true);
      return;
    }
    if (info.file.status === 'done' || info.file.status === 'error' || info.file.originFileObj) {
      getBase64(info.file.originFileObj || info.file, (url) => {
        setImageUrl(url);
        localStorage.setItem('profile_picture', url);
        setUploadLoading(false);
        notification.success({ message: 'Foto profil berhasil diperbarui!' });
        window.dispatchEvent(new Event('profilePictureUpdated'));
      });
    }
  };

  const customUploadRequest = ({ file, onSuccess }) => {
    setTimeout(() => {
      onSuccess("ok");
    }, 0);
  };

  const enterLoading = index => {
    setLoadings(prevLoadings => {
      const newLoadings = [...prevLoadings];
      newLoadings[index] = true;
      return newLoadings;
    });
  }

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleOk = async (index) => {
    enterLoading(index)
    await axios.post(`${process.env.REACT_APP_API_GATEWAY_URL}account/change-password`, {
      old_password: oldPassword,
      new_password: newPassword,
      confirm_new_password: confirmPassword,
    }).then((response) => {
      notification.success({
        message: 'Password berhasil diganti',
      });
      setOldPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setIsModalVisible(false);
      setLoadings(prevLoadings => {
        const newLoadings = [...prevLoadings];
        newLoadings[index] = false;
        return newLoadings;
      });
      form.resetFields();
    }).catch((error) => {
      setIsModalVisible(false);
      setOldPassword("");
      setNewPassword("");
      setConfirmPassword("");
      if (error.response.data.message.search("Invalid password") >= 0) {
        notification.error({
          message: 'Password lama anda salah'
        });
      } else if (error.response.data.message.search("New password is not the same as the confirmation of new password") >= 0) {
        notification.error({
          message: 'Password baru dan konfirmasi password berbeda'
        });
      } else {
        notification.error({
          message: 'Ganti password gagal'
        });
      }
      form.resetFields();
      setLoadings(prevLoadings => {
        const newLoadings = [...prevLoadings];
        newLoadings[index] = false;
        return newLoadings;
      });
    });

  };

  const handleCancel = () => {
    setIsModalVisible(false);
  };

  function programStudi(id_prodi) {
    if (id_prodi === "0") {
      return "D3 - Teknik Informatika"
    } else {
      return "D4 - Teknik Informatika"
    }
  }

  return (
    <>
      <CCard className="mb-4">
        <CCardHeader style={{ paddingLeft: "20px" }}>
          <h5><b>Profil Saya</b></h5>
        </CCardHeader>
        <CCardBody style={{ paddingLeft: "20px" }}>
          <div className="d-flex mb-4 align-items-center">
            <div style={{ marginRight: '20px' }}>
              <Avatar size={100} src={imageUrl} icon={<UserOutlined />} style={{ border: '1px solid #d9d9d9' }}/>
            </div>
            <div>
              <Upload
                name="avatar"
                showUploadList={false}
                beforeUpload={beforeUpload}
                onChange={handleUploadChange}
                customRequest={customUploadRequest}
              >
                <Button icon={uploadLoading ? <LoadingOutlined /> : <UploadOutlined />}>
                  Ubah Foto Profil
                </Button>
              </Upload>
              <div style={{ marginTop: '8px', color: '#888', fontSize: '12px' }}>Format JPG/PNG, Maks. 2MB</div>
            </div>
          </div>
          <div>
            <CRow><CCol span={24}><b> Nama </b> </CCol></CRow>
            <CRow className="card-title mb-2"><CCol span={24}> {localStorage.getItem('name')} </CCol></CRow>
            {localStorage.getItem('id_prodi') !== "undefined" && (
              <><CRow><CCol span={24}><b> Program Studi </b> </CCol></CRow>
                <CRow className="card-title mb-2"><CCol span={24}> {programStudi(localStorage.getItem('id_prodi'))} </CCol></CRow>
              </>
            )}
            <CRow><CCol span={24}><b> Username </b> </CCol></CRow>
            <CRow className="card-title mb-2"><CCol span={24}> {localStorage.getItem('username')}  </CCol></CRow>
            <CRow><CCol span={24}><b> Password </b> </CCol></CRow>
            <CRow className="card-title mb-2"><CCol span={24}> ******** </CCol></CRow>
            <div className='d-flex justify-content-end'>
              <CButton size="sm" color="primary" shape="rounded-pill" style={{ color: "white" }} onClick={showModal}>
                Ganti Password
              </CButton>
            </div>
          </div>
        </CCardBody>
      </CCard>

      <Modal title="Ganti Password"
        visible={isModalVisible}
        onOk={form.submit}
        onCancel={handleCancel}
        width={600}
        zIndex={9999999}
        footer={[
          <Button key="back" onClick={handleCancel}>
            Batal
          </Button>,
          <Button loading={loadings[0]} key="submit" type="primary" onClick={form.submit}>
            Ganti Password
          </Button>]}>
        <Form
          form={form}
          name="basic"
          wrapperCol={{ span: 24 }}
          onFinish={() => handleOk(0)}
          autoComplete="off"
        >
          <b>Password Lama<span style={{ color: "red" }}> *</span></b>
          <Form.Item
            name="oldPassword"
            rules={[{ required: true, message: 'Password lama tidak boleh kosong!' }]}
          >
            <Input.Password onChange={e => setOldPassword(e.target.value)} />
          </Form.Item>

          <b>Password Baru<span style={{ color: "red" }}> *</span></b>
          <Form.Item
            name="newPassword"
            rules={[{ required: true, message: 'Password baru tidak boleh kosong!' }]}
          >
            <Input.Password onChange={e => setNewPassword(e.target.value)} />
          </Form.Item>

          <b>Konfirmasi Password Baru<span style={{ color: "red" }}> *</span></b>
          <Form.Item
            name="confirmPassword"
            rules={[{ required: true, message: 'Konfirmasi password tidak boleh kosong!' }]}
          >
            <Input.Password onChange={e => setConfirmPassword(e.target.value)} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  )
}

export default Profile
