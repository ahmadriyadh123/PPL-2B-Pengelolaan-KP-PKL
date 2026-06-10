import 'react-app-polyfill/stable'
import 'core-js'
import React from 'react'
import ReactDOM from 'react-dom'
import App from './App'
import * as serviceWorker from './serviceWorker'
import { Provider } from 'react-redux'
import store from './store'

import { ConfigProvider } from 'antd';
import idID from 'antd/locale/id_ID';
import 'moment/locale/id'; // ensure moment is also in indonesian

ReactDOM.render(
  <Provider store={store}>
    <ConfigProvider locale={idID}>
      <App />
    </ConfigProvider>
  </Provider>,
  document.getElementById('root'),
)

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
serviceWorker.unregister()
