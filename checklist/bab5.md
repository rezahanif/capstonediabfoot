\section{Pengujian dan Pembahasan}
\label{sec:pengujian}


Pada bagian ini mahasiswa melakukan pengujian terhadap rancangan yang diajukan di \ref{chap:Detail_Implementasi}. Mahasiswa harus menuliskan hal-hal diantaranya: 
\begin{itemize}
    \item Tujuan pengujian,
    \item Hal atau bagian apa yang diuji,
    \item Skenario pengujian,
    \item Analisis dari hasil pengujian. Analisis harus dikaitkan dengan ilmu-ilmu dasar (matematika, fisika, dan \textit{engineering science}) kenapa suatu fenomena terjadi. Analisis juga merupakan cara untuk memastikan rancangan yang diajukan di \ref{chap:Detail_Implementasi} memenuhi spesifikasi atau kriteria sukses yang disebutkan,
    \item Hal-hal penting lainnya.
\end{itemize}

\subsection{Skenario Pengujian 1: Respons Sensor Suhu NTC terhadap Temperatur}

Pengujian respons sensor suhu NTC bertujuan untuk mengevaluasi kemampuan sensor NTC dalam mengukur temperatur secara akurat pada rentang temperatur yang relevan dengan kondisi pemantauan plantar kaki. Parameter keberhasilan pengujian ditentukan berdasarkan nilai Mean Absolute Error (MAE) terhadap alat ukur temperatur referensi, dengan target nilai MAE $\leq 1,^{\circ}\mathrm{C}$. Pengujian dilakukan dengan memberikan beberapa kondisi temperatur pada sensor NTC dan secara bersamaan melakukan pengukuran menggunakan termometer referensi. Setiap titik temperatur diukur beberapa kali untuk memperoleh data pembacaan sensor yang representatif. Data hasil pengukuran kemudian dicatat dan dibandingkan antara temperatur yang terbaca oleh sensor NTC dengan temperatur yang diukur menggunakan alat referensi.

Sebelum dilakukan pengujian validasi, data pengukuran dapat digunakan untuk melakukan proses kalibrasi sensor dengan menentukan hubungan antara keluaran sensor dan temperatur referensi. Parameter hasil kalibrasi kemudian diterapkan pada sistem untuk mengonversi keluaran sensor menjadi nilai temperatur. Selanjutnya, pengujian validasi dilakukan menggunakan data pengukuran yang tidak digunakan dalam proses kalibrasi untuk mengevaluasi kinerja sensor secara independen. Besarnya kesalahan pengukuran pada setiap titik dihitung menggunakan persamaan:

\begin{equation}
e_i = \left|T_{\mathrm{NTC},i}-T_{\mathrm{ref},i}\right|
\end{equation}

Nilai MAE kemudian dihitung menggunakan persamaan:

\begin{equation}
MAE = \frac{1}{N}\sum_{i=1}^{N}
\left|T_{\mathrm{NTC},i}-T_{\mathrm{ref},i}\right|
\end{equation}

dengan $T_{\mathrm{NTC},i}$ merupakan temperatur hasil pengukuran sensor NTC pada pengukuran ke-$i$, $T_{\mathrm{ref},i}$ merupakan temperatur hasil pengukuran alat referensi, dan $N$ merupakan jumlah data pengukuran. Hasil pengujian selanjutnya dibandingkan dengan parameter keberhasilan yang telah ditentukan, yaitu MAE $\leq 1,^{\circ}\mathrm{C}$. Apabila nilai MAE memenuhi batas tersebut, sensor NTC dinyatakan memiliki tingkat kesalahan pengukuran yang dapat diterima untuk digunakan sebagai bagian dari sistem pemantauan temperatur plantar pada insole DFU.
\label{subsec:Skenario1}
Sesuaikan jumlah skenario sesuai kebutuhan pengujian anda

\subsection{Skenario Pengujian 2: Respons Sensor Tekanan terhadap Variasi Beban}
\label{subsec:Skenario2}
Pengujian respons sensor tekanan bertujuan untuk mengevaluasi kemampuan sensor tekanan dalam mengukur beban yang diberikan pada permukaan insole secara akurat dan konsisten. Pengujian dilakukan dengan memberikan beberapa variasi beban yang telah diketahui nilainya pada titik pengukuran sensor. Nilai beban hasil pembacaan sensor kemudian dibandingkan dengan nilai beban referensi untuk mengetahui tingkat kesalahan pengukuran. Pada setiap variasi beban, pengukuran dilakukan secara berulang untuk mengevaluasi konsistensi atau repeatability sensor.

Besarnya kesalahan relatif pada setiap variasi beban dihitung menggunakan persamaan:

\begin{equation}
Error_sensor_i =
\frac{\left|F_{\mathrm{sensor},i}-F_{\mathrm{ref},i}\right|}
{F_{\mathrm{ref},i}}
\times 100\%
+--------------------------------------------+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++-\end{equation}

dengan $F_{\mathrm{sensor},i}$ merupakan nilai beban yang diperoleh dari sensor pada pengukuran ke-$i$, sedangkan $F_{\mathrm{ref},i}$ merupakan nilai beban referensi. Selain kesalahan pengukuran, konsistensi hasil pembacaan pada setiap variasi beban dievaluasi menggunakan koefisien variasi (Coefficient of Variation/CV). Nilai CV dihitung menggunakan persamaan:

\begin{equation}
CV =
\frac{\sigma}{\bar{F}}
\times 100\%
\end{equation}

dengan $\sigma$ merupakan standar deviasi hasil pengukuran dan $\bar{F}$ merupakan nilai rata-rata hasil pengukuran pada suatu variasi beban. Hasil pengujian kemudian dibandingkan dengan parameter keberhasilan yang telah ditentukan. Nilai error digunakan untuk mengetahui tingkat kesesuaian hasil pengukuran sensor terhadap beban referensi, sedangkan nilai CV digunakan untuk mengetahui tingkat konsistensi pembacaan sensor pada pengulangan beban yang sama. Sensor dinyatakan memiliki kinerja yang baik apabila nilai kesalahan pengukuran dan CV berada dalam batas yang telah ditentukan.

\subsection{Skenario Pengujian 3: Respons Sensor IMU terhadap Aktivitas Pengguna}
\label{subsec:Skenario3}
Pengujian respons sensor IMU bertujuan untuk mengevaluasi kemampuan sensor MPU6050 dalam mendeteksi pergerakan kaki dan menghitung jumlah langkah selama aktivitas berjalan. Pengujian dilakukan dengan memasang insole pada alas kaki pengguna dan melakukan aktivitas berjalan pada lintasan yang telah ditentukan. Selama aktivitas berlangsung, data gerakan dari sensor IMU digunakan oleh sistem untuk mendeteksi langkah dan menghitung jumlah langkah yang dilakukan pengguna. Hasil perhitungan jumlah langkah dari sistem kemudian dibandingkan dengan jumlah langkah yang dihitung secara manual sebagai nilai referensi.

Perbedaan antara jumlah langkah hasil deteksi sistem dan jumlah langkah hasil perhitungan manual digunakan untuk menentukan tingkat kesalahan deteksi jumlah langkah. Persentase error jumlah langkah dihitung menggunakan persamaan:

\begin{equation}
Error_{\mathrm{step}} =
\frac{\left|N_{\mathrm{sensor}}-N_{\mathrm{manual}}\right|}
{N_{\mathrm{manual}}}
\times 100\%
\end{equation}

dengan $N_{\mathrm{sensor}}$ merupakan jumlah langkah yang dideteksi oleh sistem dan $N_{\mathrm{manual}}$ merupakan jumlah langkah hasil perhitungan manual. Pengujian dilakukan pada beberapa jumlah langkah untuk mengetahui konsistensi kinerja sistem pada durasi atau jarak berjalan yang berbeda. Hasil pengujian kemudian dibandingkan dengan parameter keberhasilan yang telah ditentukan. Semakin kecil nilai error jumlah langkah, semakin sesuai hasil deteksi sistem terhadap jumlah langkah aktual yang dilakukan pengguna.

\subsection{Skenario Pengujian 4: Deteksi Akivitas dan Fase Gait}
\label{subsec:Skenario4}
Pengujian deteksi aktivitas dan fase gait bertujuan untuk mengevaluasi kemampuan sistem dalam mengenali pola pergerakan kaki berdasarkan data yang diperoleh dari sensor IMU. Pengujian dilakukan dengan meminta pengguna melakukan beberapa kondisi aktivitas yang telah ditentukan, seperti kondisi diam, berdiri, dan berjalan. Data akselerasi dan kecepatan sudut yang diperoleh dari sensor IMU kemudian diproses menggunakan algoritma deteksi aktivitas dan Finite State Machine (FSM) yang telah dirancang. Hasil keluaran sistem diamati untuk mengetahui apakah sistem mampu membedakan kondisi aktivitas dan mendeteksi pola langkah secara konsisten.

Pada kondisi berjalan, keluaran sistem berupa perubahan keadaan atau fase gait diamati untuk memastikan bahwa urutan fase yang terdeteksi sesuai dengan pola pergerakan kaki. Pola tersebut dapat digunakan untuk mengidentifikasi kejadian langkah dan membedakan kondisi stance dan swing sesuai dengan logika FSM yang telah dirancang. Pengujian dilakukan dengan beberapa pengulangan aktivitas untuk mengetahui konsistensi hasil deteksi. Hasil pengujian kemudian dibandingkan dengan pola aktivitas yang dilakukan secara aktual. Sistem dinyatakan mampu mendeteksi aktivitas dan fase gait apabila keluaran FSM mengikuti pola pergerakan yang dilakukan pengguna serta tidak menghasilkan perpindahan keadaan yang tidak sesuai selama aktivitas berlangsung.

\subsection{Skenario Pengujian 5: Kinerja Komunikasi Data BLE}
\label{subsec:Skenario5}
Pengujian kinerja komunikasi Bluetooth Low Energy (BLE) bertujuan untuk mengevaluasi kemampuan sistem dalam mengirimkan data hasil pengukuran dari mikrokontroler pada insole menuju perangkat penerima secara andal dan tepat waktu. Pengujian dilakukan dengan menjalankan sistem dan mengirimkan paket data sensor secara kontinu melalui komunikasi BLE. Data yang dikirim oleh mikrokontroler kemudian dibandingkan dengan data yang berhasil diterima oleh perangkat penerima untuk mengetahui jumlah paket yang berhasil diterima dan paket yang mengalami kehilangan selama proses transmisi.

Persentase packet loss dihitung menggunakan persamaan:

\begin{equation}
Packet\ Loss =
\frac{N_{\mathrm{sent}}-N_{\mathrm{received}}}
{N_{\mathrm{sent}}}
\times 100\%
\end{equation}

dengan $N_{\mathrm{sent}}$ merupakan jumlah paket data yang dikirim oleh mikrokontroler dan $N_{\mathrm{received}}$ merupakan jumlah paket yang berhasil diterima oleh perangkat penerima. Selain packet loss, pengujian dilakukan untuk mengetahui latensi pengiriman data, yaitu waktu yang dibutuhkan sejak data dikirim oleh mikrokontroler hingga data tersebut diterima oleh perangkat penerima. Latensi setiap paket dihitung menggunakan persamaan:

\begin{equation}
L_i = t_{\mathrm{received},i}-t_{\mathrm{sent},i}
\end{equation}

dengan $t_{\mathrm{sent},i}$ merupakan waktu pengiriman paket ke-$i$ dan $t_{\mathrm{received},i}$ merupakan waktu penerimaan paket ke-$i$. Nilai latensi rata-rata kemudian dihitung menggunakan:

\begin{equation}
\bar{L} =
\frac{1}{N}
\sum_{i=1}^{N} L_i
\end{equation}

Pengujian dilakukan selama sistem mengirimkan data sensor secara kontinu untuk memperoleh jumlah sampel yang memadai dalam mengevaluasi keandalan komunikasi. Hasil pengujian kemudian dibandingkan dengan parameter keberhasilan yang telah ditentukan, yaitu packet loss maksimum sebesar $5\%$ dan latensi pengiriman maksimum sebesar $200\,\mathrm{ms}$. Sistem komunikasi BLE dinyatakan memiliki kinerja yang baik apabila persentase packet loss dan latensi pengiriman berada di bawah batas tersebut sehingga data hasil pemantauan dapat diterima oleh perangkat penerima secara kontinu dan tepat waktu.

\subsection{Skenario Pengujian 6: Pemantauan Insole pada Skenario Pemakaian}
\label{subsec:Skenario7}
Pengujian pemantauan insole pada skenario pemakaian bertujuan untuk mengevaluasi kemampuan sistem dalam melakukan akuisisi, pemrosesan, dan pengiriman data secara terintegrasi ketika insole digunakan pada aktivitas yang merepresentasikan penggunaan sehari-hari. Pengujian dilakukan dengan memasang insole pada alas kaki pengguna dan menjalankan sistem secara kontinu selama pengguna melakukan beberapa aktivitas, yaitu duduk, berdiri, dan berjalan. Selama pengujian berlangsung, sensor suhu NTC, sensor tekanan, dan sensor IMU dioperasikan secara bersamaan untuk memperoleh data kondisi plantar dan aktivitas kaki.

Data hasil pengukuran diproses oleh mikrokontroler sesuai dengan algoritma yang telah dirancang, kemudian dikirimkan melalui komunikasi Bluetooth Low Energy (BLE) menuju perangkat penerima. Pengujian dilakukan dengan mengamati kontinuitas akuisisi data, kestabilan pembacaan sensor, keberlangsungan komunikasi BLE, serta kesesuaian keluaran sistem terhadap aktivitas yang dilakukan pengguna. Selain itu, diperiksa pula apakah terjadi kehilangan data, gangguan pembacaan, atau kegagalan sistem selama insole digunakan.

Skenario pengujian dilakukan dengan memberikan beberapa kondisi aktivitas secara berurutan, yaitu kondisi duduk, berdiri, dan berjalan. Setiap aktivitas dilakukan selama durasi yang telah ditentukan dan diulang beberapa kali untuk mengetahui konsistensi kinerja sistem. Hasil pengujian kemudian digunakan untuk mengevaluasi kemampuan sistem dalam melakukan pemantauan secara kontinu pada kondisi penggunaan yang mendekati implementasi sebenarnya. Sistem dinyatakan mampu melakukan pemantauan apabila seluruh sensor dapat melakukan akuisisi data selama pengujian, data dapat diproses dan dikirimkan menuju perangkat penerima, serta tidak terjadi kegagalan sistem yang mengganggu proses pemantauan selama aktivitas berlangsung.

\subsection{Skenario Pengujian 7: Hasil Deteksi dan Klasifikasi Risiko DFU}
\label{subsec:Skenario7}
Pengujian hasil deteksi dan klasifikasi risiko DFU bertujuan untuk mengevaluasi kemampuan sistem dalam mengolah data hasil pemantauan menjadi informasi tingkat risiko Diabetic Foot Ulcer (DFU). Pengujian dilakukan menggunakan data yang diperoleh dari sensor suhu, tekanan, dan IMU pada skenario pemakaian. Data tersebut diproses untuk memperoleh parameter yang digunakan dalam algoritma deteksi dan penilaian risiko yang telah dirancang. Selanjutnya, parameter hasil pengolahan digunakan untuk menghasilkan nilai atau kategori risiko sesuai dengan aturan klasifikasi yang telah ditentukan.

Pengujian dilakukan dengan memberikan beberapa kondisi pemakaian yang merepresentasikan kondisi dengan tingkat risiko yang berbeda berdasarkan parameter yang digunakan dalam sistem. Untuk setiap kondisi pengujian, hasil klasifikasi sistem dicatat dan dibandingkan dengan kondisi yang telah ditentukan berdasarkan kriteria atau aturan klasifikasi sistem. Pengujian dilakukan secara berulang untuk mengetahui konsistensi keluaran sistem dalam menghasilkan kategori risiko yang sama pada kondisi yang serupa.

Apabila sistem menggunakan beberapa kategori risiko, seperti risiko rendah, risiko sedang, dan risiko tinggi, maka hasil pengujian dapat disajikan dalam bentuk tabel perbandingan antara kondisi pengujian, parameter yang terukur, nilai risk score, dan kategori risiko yang dihasilkan sistem. Apabila tersedia nilai acuan atau label kondisi pengujian, kinerja klasifikasi dapat dievaluasi menggunakan parameter accuracy, precision, recall, dan F1-score. Namun, apabila belum tersedia ground truth klinis, evaluasi dibatasi pada kesesuaian keluaran sistem terhadap kriteria dan skenario pengujian yang telah didefinisikan, sehingga hasil pengujian tidak digunakan untuk menyatakan kemampuan sistem dalam mendiagnosis DFU secara klinis.

Selain menghasilkan klasifikasi risiko, pengujian dilakukan untuk memastikan bahwa setiap kategori risiko menghasilkan informasi tindak lanjut yang sesuai dengan rancangan sistem. Hasil keluaran sistem dapat berupa informasi untuk melanjutkan pemantauan, memberikan peringatan kepada pengguna, melakukan pengurangan beban atau aktivitas yang berpotensi meningkatkan risiko, atau menyarankan pengguna untuk melakukan evaluasi lebih lanjut oleh tenaga kesehatan apabila kondisi berisiko ditemukan secara berulang atau menetap. Dengan demikian, pengujian ini mengevaluasi tidak hanya kemampuan sistem dalam menghasilkan klasifikasi risiko, tetapi juga keterhubungan antara hasil deteksi dengan informasi tindak lanjut yang diberikan kepada pengguna.

\subsection{Skenario Pengujian 8: Durasi Operasi Kontinu (better ada ga ya?)}
\label{subsec:Skenario8}
Pengujian durasi operasi kontinu bertujuan untuk mengevaluasi kemampuan sistem insole dalam beroperasi secara terus-menerus menggunakan sumber daya baterai yang tersedia. Pengujian dilakukan dengan mengoperasikan seluruh fungsi utama sistem, meliputi akuisisi data sensor, pemrosesan data, deteksi aktivitas, serta komunikasi Bluetooth Low Energy (BLE), secara bersamaan hingga sistem tidak dapat menjalankan fungsi utamanya akibat penurunan tegangan atau habisnya kapasitas baterai.

Sebelum pengujian dimulai, baterai diisi hingga kondisi penuh dan sistem diatur pada kondisi operasi yang sama dengan skenario penggunaan normal. Waktu mulai pengoperasian dicatat sebagai waktu awal pengujian. Selama pengujian berlangsung, sistem dioperasikan secara kontinu dengan konfigurasi sensor dan komunikasi yang sama dengan konfigurasi penggunaan sebenarnya. Waktu ketika sistem berhenti melakukan akuisisi data, kehilangan koneksi secara permanen, atau tidak lagi dapat menjalankan fungsi utamanya dicatat sebagai waktu akhir pengujian.

Durasi operasi kontinu dihitung berdasarkan selisih antara waktu akhir dan waktu awal pengujian menggunakan persamaan:

\begin{equation}
t_{\mathrm{operasi}} =
t_{\mathrm{akhir}}-t_{\mathrm{awal}}
\end{equation}

dengan $t_{\mathrm{operasi}}$ merupakan durasi operasi sistem, $t_{\mathrm{awal}}$ merupakan waktu ketika sistem mulai beroperasi menggunakan baterai penuh, dan $t_{\mathrm{akhir}}$ merupakan waktu ketika sistem tidak lagi dapat menjalankan fungsi utamanya.

Pengujian dilakukan beberapa kali menggunakan kondisi awal baterai yang sama untuk mengetahui konsistensi durasi operasi sistem. Nilai rata-rata durasi operasi dihitung menggunakan persamaan:

\begin{equation}
\bar{t}_{\mathrm{operasi}} =
\frac{1}{N}
\sum_{i=1}^{N} t_{\mathrm{operasi},i}
\end{equation}

dengan $N$ merupakan jumlah pengulangan pengujian. Selain durasi operasi, tegangan baterai dapat dipantau selama pengujian untuk mengetahui karakteristik penurunan tegangan selama sistem beroperasi. Hasil pengujian kemudian dibandingkan dengan durasi operasi minimum yang ditetapkan sebagai parameter keberhasilan. Sistem dinyatakan memenuhi kebutuhan operasi apabila mampu menjalankan seluruh fungsi utama secara kontinu selama durasi minimum yang telah ditentukan tanpa mengalami kegagalan sistem.
\section{\textit{Improvement}}
\label{sec:improvement}
Di dalam Bab ini, Anda harus secara rinci menjelaskan kenapa perlu tambahan dan/atau revisi rancangan secara logis dan kronologis sehingga tim penilai akan melihat bahwa tambahan ini adalah sesuatu yang perlu dan wajar untuk dilakukan. 


