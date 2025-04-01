package com.example.messengerapp.fragments
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Arrays


class AccessToken {
    private val  CREDENTIALS_FILE_PATH = "messenger-app-fb9e8-firebase-adminsdk-fbsvc-f520554428.json"
    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"

    @Throws(IOException::class)
    fun getAccessToken(): String {
        try {
            val jsonString = "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"messenger-app-fb9e8\",\n" +
                "  \"private_key_id\": \"f520554428fe46145798ef6d35db35a3038a766f\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCXkmKE7ictBm7a\\ncOKQz9V9uzcLRlMcQ8RxTkVUP/dWZKUq79fJ02m4UczBljE9fO6oqzFHsfleJ3Ge\\nV/esv4kvySzrm+jZ7HtDkj3S5sY8T2/5K31y9qXkw01HgEJQAV3POjjsFE5nVLvr\\nJIQe2bf1Kiq8ANcmhBTaJUowM/oI/bB2+fOV77Xn5ExEiTDM74E3Ct0opjt6j3xS\\nzpc3AsotlnQQC8Wtn/w8J/SdHUY2XRHHJMzS4fDxhnEJIXXb9TT6sqtiPXS7loV1\\nRAHG01q+uR4oa6o7cLShnntlT63GU/a1GuAV3fUfiR+JvoZRQ8aQpuoMW4ebJmw0\\ncginDlAzAgMBAAECggEAIyChZYZeHPC7OzPjRkVsiIWddab4Nvo7qjjGF4b1lxef\\nMcrBG3y8KSQIDKt2wPXVx9BZDPEhxmDk05o2g9ol/KWmm/oXpn3IeXoIivrB4ZNV\\nc41RNxYTj2d13wbKF4mhiJfJPmlcXFVGcfIWDqJNoGuD2Otm8X216XKv9WjuXyu0\\n5zdTpHha6KdFgAe2nKC6eQe6jVjN9xqdiPDBs4p8cc0f+CVu7MI+r5Rthd/IJRDI\\nCaA1Ep/3sCuTDTDBrQUOeusX2EMOQXoinzaJeiXj18p7prEDmV5lidAJh1c/UENj\\ntCCAWQjCwoNRutQk6POgZaXXWwGnv2VPh70CcIbHeQKBgQDJ7cLVrhIsjBS/ivnz\\n1IccGmUmo4dcs7GIWdMpn1Yt76Pr33jP8W9aqzeEuPZNymx+fU1GgUYe//13w266\\nGDBAZ3A0HBX9tfFeQcq6sOUf6LVK0n4mNVS7Uz7Vml1fpbvBrcdbWxTyHrEh3uQT\\ny4tLegzpsUdOU1LGlxpxa9pA2QKBgQDAKKZi9H+FryNbnx58NmLDhSCO5sE4KOBr\\nf3QmDAl7F5FVejKdyIIMqA3BHjzDbrsAHQd04HhCbjS84qDpZQQPWX8uXMRBxunG\\nqLR8T2hrVTI6WX+MuztM1/4EvyS5UMvIl7W9lsGQuy5m0oX5p5CHZ5F7MChtrnR6\\nK58rmp1x6wKBgEf5t7jykwfN5pdfsjh3SLF7txJOt7ETW/R+EAaGBPHauVMaFLYK\\nGOb4oJFuXFzeCk4L27+F1Li4BmDqCE3M0WYjeJsaZ7xHXgF68ggZhZIaIITLAqwb\\n1lfvFDaPDzlImeQvTSADFzQ2PG0WRbXIl54PowqpZgFFMfwXYBWNm/4BAoGAFTmo\\nP0/wcgzJZApsbJ35RM2EriQw/YoOV/FpG/bCaVVOGVn5E1KdgtZhRo6XeaLWhwmt\\n1CwjPHs92gtSaNVeoG9rS+y1ROq60dW6sex3hTuQ478VBq97ul41UcLnRIz3JX4U\\ndg+b6+exFWx1ngDzRvGasTe9j3fwp7n/x+o5L+cCgYEAxpJhN3mXED+Ga6gcahmX\\nNcbKNuAnDSU/ShVvwX9FwU+X36mxG9VB1CAXeX6qAWHvrM5hVDp5Ta1DwKlh/4nt\\naF+gXaEC6PumaFMLpodI3egVErgYLk0ogJQw0e/KB/Gs0U/cqr5BilH+TAGY3cxc\\na0jYIKsi49z7HIUmWkJiuE8=\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"firebase-adminsdk-fbsvc@messenger-app-fb9e8.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"111236904400462034169\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40messenger-app-fb9e8.iam.gserviceaccount.com\",\n" +
                "  \"universe_domain\": \"googleapis.com\"\n" +
                "}"
            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val credentials = GoogleCredentials.fromStream(stream)
                .createScoped(Arrays.asList(firebaseMessagingScope))
            credentials.refreshIfExpired()
            return credentials.accessToken.tokenValue
        } catch (e: IOException) {
            throw e
        }
    }
}