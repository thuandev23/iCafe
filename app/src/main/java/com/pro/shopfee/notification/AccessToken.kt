package com.pro.shopfee.notification

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class AccessToken {
    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"

    fun getAccessToken(): String? {
        try {
            val jsonString ="{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"cafem-80e6f\",\n" +
                    "  \"private_key_id\": \"e0af24feaa417032da8093b5ebcdd00e97b3aa0f\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDMIqHXQ7z8VHFS\\nUgCDmAmkdZZSkgHVpX4pHc2lRE04wnddGJx80b1Oid0GxZTpj1dgeoowDgDZMxSk\\n66EJMOy8tUEK862mfActZSv0xd6bq2HVJ+g3WdZ+u7/Yy+k2uxEoRcPB2lW5oZRS\\nMIg2h39EVM3cpLL8CGEVbY2fDtLRMmdVA8QaRUnVwbpm4k6acG+6TfSRWkxSjDw2\\nSLTM/9CgnMdoPmzBJrBvUgRRvCu/OXRbaOOjiuz2ZnfXdczuimaQ+1PRUXX//pjK\\nH3w04xC/EKrXj0vIkzqXkX0xClmpvDtoLTmn+COPWrxlWtiJzdLrU9ki8RyQhSPA\\n0aLzX8rHAgMBAAECggEAPJxv113KiFV+TEYC1w/WlRlA6AVoPApmWkj6HX8ZOqgr\\nDt5W4IzaG669jNwvqeWguQnJbBlMUTFkiM9ZwkPfluxugpT103/yFZ17nrL9tNT/\\nOG4f+gdXs8uFXBa3iUOkBYQpaq6sU3DRfFHir+YteJmBgmj4p/LI+LbNnROmruwz\\nlGGGOUqyi2OJSAddELIE7mvEt2v53bZkYccwdw0qdePkUN2ukbmXPu8fFz3kAf56\\nFyVp5R7t4As/gsojRkK/HGtm0feaT/bgfWCFf7ksCSsGgyDab34NeD/g4rl6dI/T\\n8pdqLCxIF0auLR5meZ2KYZGE/bV37yd9E9D/UCgw0QKBgQDnqxNAL8pLLbJEJpyQ\\n0zNlimhW168nY9WKxEAJapPJ9O0wMNe7xyBdja4hPVMKTTW03z8alDUJL9gLlfi7\\noVuhMr1JfLBdSRNsspUObTnFE5migPhwM3z3LhJwzcZ1ID/M0Xr9eI5fWk/01YIe\\ng2/FvRLNzG3rUYhP03fQEcybDwKBgQDhk0VFRSbWvY5XaAIAIryDC1PcR2aV07pB\\n1C6pENxHQIbYIXGrtX5ic3fXQs/IV5q8m9y7Hu1yBGaDiow1imjqPelhE1TGkzKu\\nefWp9vy/FOvtONNu3vNjO27aC6Rw9lV/QGpQqUG+KUwDdpRp4Q6hK2nphjxkZqj+\\n4IXeLhU0yQKBgEUq97lvS1e80f+1A4Pd+vdzJonfJaV+EjC3PWNSVcOzg+mie5bN\\n8iTce0PjWlq2CLXy/ZVC8L7IcRmscjIbEA+F5av+NW30tn76prRC+j+UBYfWN9bQ\\nGJx4Pfs+BOXNdh8B4FWzW9auUTcJVCsmFQYV2Hpncd4TXr5updQSrGMTAoGBAJ26\\nPUh3VvX2a05Awo1XvJqg1A2Fu3xuJKGc/Ndom64St3buw6295VZvyM8yv1klsGPL\\nVCdfM8Zyvz+hI/LYCg4oJkSjoLaM/HY1fwpOUmJF/cXM40J2EJdFWniKhXwbMqMc\\n5nnqL7cI50bQpmv2WyVk1tTb8OR/SBGX6uL1NAzJAoGAIbhVYpGJqpFMWSNc4tPH\\niPTcvmR06dOtjZmCgTUHGzTHcnjUCD4mQ0mhiopfM+FkRY7VfV+TI+0rvWoHNiYd\\nUgLA+M+7QKPrw5PTskp8JszVeaYX3BoqIVQI7CH4rYQuH8Rg2ldthB8FoO33kO3r\\ndC5yNWujB4LIV9ar3LNw0bc=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-thism@cafem-80e6f.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"105149204055237999223\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-thism%40cafem-80e6f.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n" +
                    "  "
            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredentials =
                GoogleCredentials.fromStream(stream).createScoped(listOf(firebaseMessagingScope))

            googleCredentials.refreshIfExpired()
            return googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}