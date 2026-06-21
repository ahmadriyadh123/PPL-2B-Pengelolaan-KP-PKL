import React, { Suspense, useState, useEffect } from 'react'
import { Redirect, Route, Switch, useLocation } from 'react-router-dom'
import { CContainer, CSpinner } from '@coreui/react'

import routes from '../routes'

const AppContent = () => {
  const location = useLocation()

  const [userRole, setUserRole] = useState(() => {
    const rawRole = localStorage.getItem('id_role')
    return rawRole !== null ? rawRole.toString() : null
  })

  useEffect(() => {
    const checkStorageManipulation = (e) => {
      if (e.key === 'id_role') {
        console.warn('ID_Role Manipulation Detected!')

        const newValue = e.newValue ? e.newValue.toString() : null

        if (newValue !== userRole) {
          localStorage.clear()
          setUserRole(null)
          window.location.href = '/login'
        }
      }
    }

    window.addEventListener('storage', checkStorageManipulation)

    return () => {
      window.removeEventListener('storage', checkStorageManipulation)
    }
  }, [userRole])

  useEffect(() => {
    const currentRoleInStorage = localStorage.getItem('id_role')
    if (currentRoleInStorage !== userRole) {
      setUserRole(currentRoleInStorage !== null ? currentRoleInStorage.toString() : null)
    }
  }, [location, userRole])

  return (
    <CContainer lg>
      <Suspense fallback={<CSpinner color="primary" />}>
        <Switch>
          {routes.map((route, idx) => {
            return (
              route.component && (
                <Route
                  key={idx}
                  path={route.path}
                  exact={route.exact}
                  name={route.name}
                  render={(props) => {
                    if (route.protected) {
                      // User belum login / data role kosong
                      if (!userRole) {
                        return <Redirect to="/login" />
                      }

                      // Role user tidak terdaftar di rute ini
                      if (route.roles && !route.roles.includes(userRole)) {
                        console.warn(`[BLOCKED] Role ${userRole} dilarang mengakses ${route.path}`)
                        return <Redirect to="/dashboard" />
                      }
                    }

                    return <route.component {...props} />
                  }}
                />
              )
            )
          })}
          <Redirect from="/" to="/dashboard" />
        </Switch>
      </Suspense>
    </CContainer>
  )
}

export default React.memo(AppContent)
