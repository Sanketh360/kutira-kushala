package com.example.mystore

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

private val Purple = Color(0xFF6C3CE1)
private val PurpleDark = Color(0xFF4A1FA8)
private val PurpleLight = Color(0xFF9B6DFF)
private val Gold = Color(0xFFFFB930)
private val Surface1 = Color(0xFFF8F5FF)

val indianStatesAndDistricts = mapOf(
    "Andhra Pradesh" to listOf("Anantapur", "Chittoor", "East Godavari", "Guntur", "Krishna", "Kurnool", "Prakasam", "Srikakulam", "Visakhapatnam", "Vizianagaram", "West Godavari", "YSR Kadapa"),
    "Arunachal Pradesh" to listOf("Tawang", "West Kameng", "East Kameng", "Papum Pare", "Kurung Kumey", "Kra Daadi", "Lower Subansiri", "Upper Subansiri", "West Siang", "East Siang", "Siang", "Upper Siang", "Lower Siang", "Lower Dibang Valley", "Dibang Valley", "Anjaw", "Lohit", "Namsai", "Changlang", "Tirap", "Longding"),
    "Assam" to listOf("Baksa", "Barpeta", "Biswanath", "Bongaigaon", "Cachar", "Charaideo", "Chirang", "Darrang", "Dhemaji", "Dhubri", "Dibrugarh", "Dima Hasao", "Goalpara", "Golaghat", "Hailakandi", "Hojai", "Jorhat", "Kamrup Metropolitan", "Kamrup", "Karbi Anglong", "Karimganj", "Kokrajhar", "Lakhimpur", "Majuli", "Morigaon", "Nagaon", "Nalbari", "Sivasagar", "Sonitpur", "South Salmara-Mankachar", "Tinsukia", "Udalguri", "West Karbi Anglong"),
    "Bihar" to listOf("Araria", "Arwal", "Aurangabad", "Banka", "Begusarai", "Bhagalpur", "Bhojpur", "Buxar", "Darbhanga", "East Champaran", "Gaya", "Gopalganj", "Jamui", "Jehanabad", "Kaimur", "Katihar", "Khagaria", "Kishanganj", "Lakhisarai", "Madhepura", "Madhubani", "Munger", "Muzaffarpur", "Nalanda", "Nawada", "Patna", "Purnia", "Rohtas", "Saharsa", "Samastipur", "Saran", "Sheikhpura", "Sheohar", "Sitamarhi", "Siwan", "Supaul", "Vaishali", "West Champaran"),
    "Chhattisgarh" to listOf("Balod", "Baloda Bazar", "Balrampur", "Bastar", "Bemetara", "Bijapur", "Bilaspur", "Dantewada", "Dhamtari", "Durg", "Gariaband", "Janjgir-Champa", "Jashpur", "Kabirdham", "Kanker", "Kondagaon", "Korba", "Koriya", "Mahasamund", "Mungeli", "Narayanpur", "Raigarh", "Raipur", "Rajnandgaon", "Sukma", "Surajpur", "Surguja"),
    "Goa" to listOf("North Goa", "South Goa"),
    "Gujarat" to listOf("Ahmedabad", "Amreli", "Anand", "Aravalli", "Banaskantha", "Bharuch", "Bhavnagar", "Botad", "Chhota Udaipur", "Dahod", "Dang", "Devbhoomi Dwarka", "Gandhinagar", "Gir Somnath", "Jamnagar", "Junagadh", "Kheda", "Kutch", "Mahisagar", "Mehsana", "Morbi", "Narmada", "Navsari", "Panchmahal", "Patan", "Porbandar", "Rajkot", "Sabarkantha", "Surat", "Surendranagar", "Tapi", "Vadodara", "Valsad"),
    "Haryana" to listOf("Ambala", "Bhiwani", "Charkhi Dadri", "Faridabad", "Fatehabad", "Gurugram", "Hisar", "Jhajjar", "Jind", "Kaithal", "Karnal", "Kurukshetra", "Mahendragarh", "Nuh", "Palwal", "Panchkula", "Panipat", "Rewari", "Rohtak", "Sirsa", "Sonipat", "Yamunanagar"),
    "Himachal Pradesh" to listOf("Bilaspur", "Chamba", "Hamirpur", "Kangra", "Kinnaur", "Kullu", "Lahaul and Spiti", "Mandi", "Shimla", "Sirmaur", "Solan", "Una"),
    "Jharkhand" to listOf("Bokaro", "Chatra", "Deoghar", "Dhanbad", "Dumka", "East Singhbhum", "Garhwa", "Giridih", "Godda", "Gumla", "Hazaribagh", "Jamtara", "Khunti", "Koderma", "Latehar", "Lohardaga", "Pakur", "Palamu", "Ramgarh", "Ranchi", "Sahibganj", "Saraikela Kharsawan", "Simdega", "West Singhbhum"),
    "Karnataka" to listOf("Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban", "Bidar", "Chamarajanagar", "Chikkaballapur", "Chikkamagaluru", "Chitradurga", "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan", "Haveri", "Kalaburagi", "Kodagu", "Kolar", "Koppal", "Mandya", "Mysuru", "Raichur", "Ramanagara", "Shivamogga", "Tumakuru", "Udupi", "Uttara Kannada", "Vijayapura", "Yadgir"),
    "Kerala" to listOf("Alappuzha", "Ernakulam", "Idukki", "Kannur", "Kasaragod", "Kollam", "Kottayam", "Kozhikode", "Malappuram", "Palakkad", "Pathanamthitta", "Thiruvananthapuram", "Thrissur", "Wayanad"),
    "Madhya Pradesh" to listOf("Agar Malwa", "Alirajpur", "Anuppur", "Ashoknagar", "Balaghat", "Barwani", "Betul", "Bhind", "Bhopal", "Burhanpur", "Chhatarpur", "Chhindwara", "Damoh", "Datia", "Dewas", "Dhar", "Dindori", "Guna", "Gwalior", "Harda", "Hoshangabad", "Indore", "Jabalpur", "Jhabua", "Katni", "Khandwa", "Khargone", "Mandla", "Mandsaur", "Morena", "Narsinghpur", "Neemuch", "Panna", "Raisen", "Rajgarh", "Ratlam", "Rewa", "Sagar", "Satna", "Sehore", "Seoni", "Shahdol", "Shajapur", "Sheopur", "Shivpuri", "Sidhi", "Singrauli", "Tikamgarh", "Ujjain", "Umaria", "Vidisha"),
    "Maharashtra" to listOf("Ahmednagar", "Akola", "Amravati", "Aurangabad", "Beed", "Bhandara", "Buldhana", "Chandrapur", "Dhule", "Gadchiroli", "Gondia", "Hingoli", "Jalgaon", "Jalna", "Kolhapur", "Latur", "Mumbai City", "Mumbai Suburban", "Nagpur", "Nanded", "Nandurbar", "Nashik", "Osmanabad", "Palghar", "Parbhani", "Pune", "Raigad", "Ratnagiri", "Sangli", "Satara", "Sindhudurg", "Solapur", "Thane", "Wardha", "Washim", "Yavatmal"),
    "Manipur" to listOf("Bishnupur", "Chandel", "Churachandpur", "Imphal East", "Imphal West", "Jiribam", "Kakching", "Kamjong", "Kangpokpi", "Noney", "Pherzawl", "Senapati", "Tamenglong", "Tengnoupal", "Thoubal", "Ukhrul"),
    "Meghalaya" to listOf("East Garo Hills", "East Jaintia Hills", "East Khasi Hills", "North Garo Hills", "Ri Bhoi", "South Garo Hills", "South West Garo Hills", "South West Khasi Hills", "West Garo Hills", "West Jaintia Hills", "West Khasi Hills"),
    "Mizoram" to listOf("Aizawl", "Champhai", "Kolasib", "Lawngtlai", "Lunglei", "Mamit", "Saiha", "Serchhip"),
    "Nagaland" to listOf("Dimapur", "Kiphire", "Kohima", "Longleng", "Mokokchung", "Mon", "Peren", "Phek", "Tuensang", "Wokha", "Zunheboto"),
    "Odisha" to listOf("Angul", "Balangir", "Balasore", "Bargarh", "Bhadrak", "Boudh", "Cuttack", "Deogarh", "Dhenkanal", "Gajapati", "Ganjam", "Jagatsinghpur", "Jajpur", "Jharsuguda", "Kalahandi", "Kandhamal", "Kendrapara", "Kendujhar", "Khordha", "Koraput", "Malkangiri", "Mayurbhanj", "Nabarangpur", "Nayagarh", "Nuapada", "Puri", "Rayagada", "Sambalpur", "Subarnapur", "Sundargarh"),
    "Punjab" to listOf("Amritsar", "Barnala", "Bathinda", "Faridkot", "Fatehgarh Sahib", "Fazilka", "Ferozepur", "Gurdaspur", "Hoshiarpur", "Jalandhar", "Kapurthala", "Ludhiana", "Mansa", "Moga", "Muktsar", "Nawanshahr", "Pathankot", "Patiala", "Rupnagar", "Sangrur", "SAS Nagar", "Tarn Taran"),
    "Rajasthan" to listOf("Ajmer", "Alwar", "Banswara", "Baran", "Barmer", "Bharatpur", "Bhilwara", "Bikaner", "Bundi", "Chittorgarh", "Churu", "Dausa", "Dholpur", "Dungarpur", "Hanumangarh", "Jaipur", "Jaisalmer", "Jalore", "Jhalawar", "Jhunjhunu", "Jodhpur", "Karauli", "Kota", "Nagaur", "Pali", "Pratapgarh", "Rajsamand", "Sawai Madhopur", "Sikar", "Sirohi", "Sri Ganganagar", "Tonk", "Udaipur"),
    "Sikkim" to listOf("East Sikkim", "North Sikkim", "South Sikkim", "West Sikkim"),
    "Tamil Nadu" to listOf("Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore", "Dharmapuri", "Dindigul", "Erode", "Kallakurichi", "Kanchipuram", "Kanyakumari", "Karur", "Krishnagiri", "Madurai", "Nagapattinam", "Namakkal", "Nilgiris", "Perambalur", "Pudukkottai", "Ramanathapuram", "Ranipet", "Salem", "Sivaganga", "Tenkasi", "Thanjavur", "Theni", "Thoothukudi", "Tiruchirappalli", "Tirunelveli", "Tirupathur", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur", "Vellore", "Viluppuram", "Virudhunagar"),
    "Telangana" to listOf("Adilabad", "Bhadradri Kothagudem", "Hyderabad", "Jagtial", "Jangaon", "Jayashankar Bhupalpally", "Jogulamba Gadwal", "Kamareddy", "Karimnagar", "Khammam", "Komaram Bheem Asifabad", "Mahabubabad", "Mahabubnagar", "Mancherial", "Medak", "Medchal", "Nagarkurnool", "Nalgonda", "Nirmal", "Nizamabad", "Peddapalli", "Rajanna Sircilla", "Rangareddy", "Sangareddy", "Siddipet", "Suryapet", "Vikarabad", "Wanaparthy", "Warangal", "Yadadri Bhuvanagiri"),
    "Tripura" to listOf("Dhalai", "Gomati", "Khowai", "North Tripura", "Sepahijala", "South Tripura", "Unakoti", "West Tripura"),
    "Uttar Pradesh" to listOf("Agra", "Aligarh", "Allahabad", "Ambedkar Nagar", "Amethi", "Amroha", "Auraiya", "Azamgarh", "Baghpat", "Bahraich", "Ballia", "Balrampur", "Banda", "Barabanki", "Bareilly", "Basti", "Bhadohi", "Bijnor", "Budaun", "Bulandshahr", "Chandauli", "Chitrakoot", "Deoria", "Etah", "Etawah", "Faizabad", "Farrukhabad", "Fatehpur", "Firozabad", "Gautam Buddha Nagar", "Ghaziabad", "Ghazipur", "Gonda", "Gorakhpur", "Hamirpur", "Hapur", "Hardoi", "Hathras", "Jalaun", "Jaunpur", "Jhansi", "Kannauj", "Kanpur Dehat", "Kanpur Nagar", "Kasganj", "Kaushambi", "Kheri", "Kushinagar", "Lalitpur", "Lucknow", "Maharajganj", "Mahoba", "Mainpuri", "Mathura", "Mau", "Meerut", "Mirzapur", "Moradabad", "Muzaffarnagar", "Pilibhit", "Pratapgarh", "Raebareli", "Rampur", "Saharanpur", "Sambhal", "Sant Kabir Nagar", "Shahjahanpur", "Shamli", "Shravasti", "Siddharthnagar", "Sitapur", "Sonbhadra", "Sultanpur", "Unnao", "Varanasi"),
    "Uttarakhand" to listOf("Almora", "Bageshwar", "Chamoli", "Champawat", "Dehradun", "Haridwar", "Nainital", "Pauri Garhwal", "Pithoragarh", "Rudraprayag", "Tehri Garhwal", "Udham Singh Nagar", "Uttarkashi"),
    "West Bengal" to listOf("Alipurduar", "Bankura", "Birbhum", "Cooch Behar", "Dakshin Dinajpur", "Darjeeling", "Hooghly", "Howrah", "Jalpaiguri", "Jhargram", "Kalimpong", "Kolkata", "Malda", "Murshidabad", "Nadia", "North 24 Parganas", "Paschim Bardhaman", "Paschim Medinipur", "Purba Bardhaman", "Purba Medinipur", "Purulia", "South 24 Parganas", "Uttar Dinajpur")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStorePage(
    viewModel: ProductManagerViewModel = viewModel(),
    onBackClick: () -> Unit,
    onStoreCreated: () -> Unit,
    initialStoreData: StoreData? = null
) {
    val context = LocalContext.current
    val isEditing = initialStoreData != null

    var storeName by remember { mutableStateOf(initialStoreData?.storeName ?: "") }
    var storeDescription by remember { mutableStateOf(initialStoreData?.storeDescription ?: "") }
    var ownerName by remember { mutableStateOf(initialStoreData?.ownerName ?: "") }
    var gender by remember { mutableStateOf(initialStoreData?.gender ?: "") }
    var phoneNumber by remember { mutableStateOf(initialStoreData?.phoneNumber ?: "") }
    var selectedStoreProducts = remember { mutableStateListOf<String>().apply { addAll(initialStoreData?.storeProducts ?: emptyList()) } }
    var showOthersField by remember { mutableStateOf(false) }
    var customProductType by remember { mutableStateOf("") }
    
    // Address parsing
    var state by remember { mutableStateOf(initialStoreData?.state ?: "") }
    var district by remember { mutableStateOf(initialStoreData?.district ?: "") }
    var taluk by remember { mutableStateOf(initialStoreData?.taluk ?: "") }
    var villageAddress by remember { mutableStateOf(initialStoreData?.address ?: "") }
    
    var storeLogoUri by remember { mutableStateOf<Uri?>(initialStoreData?.storeLogo) }
    val storeLogoUrl = initialStoreData?.storeLogoUrl
    var isLoading by remember { mutableStateOf(false) }

    // Validation
    val isValid = storeName.isNotBlank() && ownerName.isNotBlank() && gender.isNotBlank() && phoneNumber.isNotBlank() && state.isNotBlank() && district.isNotBlank()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) storeLogoUri = uri }

    Scaffold(
        containerColor = Surface1,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(PurpleDark, Purple, PurpleLight)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .padding(top = 24.dp), // handle status bar if needed
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(Color.White.copy(.15f), CircleShape).size(40.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isEditing) "Edit Store" else "Create Store",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            if (isEditing) "Update your store information" else "Set up your marketplace store",
                            fontSize = 12.sp,
                            color = Color.White.copy(.7f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {

            // ── Logo upload hero section ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Brush.verticalGradient(listOf(Purple, PurpleLight.copy(.4f), Surface1)))
            ) {
                // Decorative circles
                Box(modifier = Modifier.size(160.dp).offset((-40).dp, (-40).dp)
                    .background(Color.White.copy(.05f), CircleShape))
                Box(modifier = Modifier.size(100.dp).align(Alignment.TopEnd).offset(30.dp, (-20).dp)
                    .background(Gold.copy(.1f), CircleShape))

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo picker circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, Gold, CircleShape)
                            .background(Color.White.copy(.15f))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (storeLogoUri != null || !storeLogoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = storeLogoUri ?: storeLogoUrl,
                                contentDescription = "Store Logo",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            // Edit overlay
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White.copy(.9f), modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("Add Logo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Tap to upload store logo", color = Color.White.copy(.7f), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Section: Store Info ──────────────────────────────────────────
            SectionHeader(icon = Icons.Default.Store, title = "Store Details")
            Spacer(Modifier.height(12.dp))

            StyledTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = "Store Name *",
                icon = Icons.Default.Storefront,
                placeholder = "e.g. Sunita's Organic Farm"
            )
            Spacer(Modifier.height(14.dp))

            StyledTextField(
                value = storeDescription,
                onValueChange = { storeDescription = it },
                label = "Store Description",
                icon = Icons.Default.Description,
                placeholder = "Tell customers what makes your store special…",
                minLines = 3
            )
            Spacer(Modifier.height(14.dp))

            // Store Products (Searchable Multi-select Dropdown)
            Text(
                "Store Products *",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color.Black.copy(.8f)
            )
            Text(
                "Select the categories of products you sell",
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))

            var expandedStoreProducts by remember { mutableStateOf(false) }
            var productSearchText by remember { mutableStateOf("") }
            val filteredStoreCategories = productCategories.filter { it.contains(productSearchText, ignoreCase = true) }

            ExposedDropdownMenuBox(
                expanded = expandedStoreProducts,
                onExpandedChange = { expandedStoreProducts = !expandedStoreProducts },
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                OutlinedTextField(
                    value = productSearchText,
                    onValueChange = { 
                        productSearchText = it 
                        expandedStoreProducts = true
                    },
                    label = { Text("Search & Select Products") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple, unfocusedBorderColor = Purple.copy(.25f),
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStoreProducts) }
                )
                ExposedDropdownMenu(
                    expanded = expandedStoreProducts,
                    onDismissRequest = { expandedStoreProducts = false }
                ) {
                    filteredStoreCategories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = selectedStoreProducts.contains(selectionOption), onCheckedChange = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(selectionOption)
                                }
                            },
                            onClick = {
                                if (selectionOption == "➕ Others") {
                                    showOthersField = !showOthersField
                                } else {
                                    if (selectedStoreProducts.contains(selectionOption)) {
                                        selectedStoreProducts.remove(selectionOption)
                                    } else {
                                        selectedStoreProducts.add(selectionOption)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (selectedStoreProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedStoreProducts) { product ->
                        Surface(
                            color = Purple.copy(.1f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Purple.copy(.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(product, fontSize = 12.sp, color = Purple, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    modifier = Modifier.size(14.dp).clickable { selectedStoreProducts.remove(product) },
                                    tint = Purple
                                )
                            }
                        }
                    }
                }
            }

            if (showOthersField) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = customProductType,
                    onValueChange = { customProductType = it },
                    label = { Text("Enter Custom Product Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple, unfocusedBorderColor = Purple.copy(.25f),
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Section: Owner ───────────────────────────────────────────────
            SectionHeader(icon = Icons.Default.Person, title = "Owner Details")
            Spacer(Modifier.height(12.dp))

            StyledTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = "Owner Name *",
                icon = Icons.Default.Person
            )
            Spacer(Modifier.height(14.dp))

            StyledTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number *",
                icon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone
            )
            Spacer(Modifier.height(14.dp))

            // Gender Dropdown
            var expandedGender by remember { mutableStateOf(false) }
            val genderOptions = listOf("Male", "Female")
            ExposedDropdownMenuBox(
                expanded = expandedGender,
                onExpandedChange = { expandedGender = !expandedGender },
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Gender *", fontSize = 13.sp, color = Color.Black.copy(.7f)) },
                    leadingIcon = { Icon(Icons.Default.PersonOutline, null, tint = Purple, modifier = Modifier.size(20.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        focusedBorderColor = Purple, unfocusedBorderColor = Purple.copy(.25f),
                        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedGender,
                    onDismissRequest = { expandedGender = false }
                ) {
                    genderOptions.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = {
                                gender = g
                                expandedGender = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            
            // ── Section: Location ──────────────────────────────────────────────
            SectionHeader(icon = Icons.Default.LocationOn, title = "Location Details")
            Spacer(Modifier.height(12.dp))

            // Country
            OutlinedTextField(
                value = "India",
                onValueChange = { },
                readOnly = true,
                enabled = false,
                label = { Text("Country", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Public, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Purple.copy(.25f),
                    disabledContainerColor = Color.White,
                    disabledLabelColor = Color.Gray
                )
            )
            Spacer(Modifier.height(14.dp))

            // State Dropdown
            var expandedState by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedState,
                onExpandedChange = { expandedState = !expandedState },
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    readOnly = true,
                    label = { Text("State *", fontSize = 13.sp, color = Color.Black.copy(.7f)) },
                    leadingIcon = { Icon(Icons.Default.Map, null, tint = Purple, modifier = Modifier.size(20.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        focusedBorderColor = Purple, unfocusedBorderColor = Purple.copy(.25f),
                        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedState,
                    onDismissRequest = { expandedState = false }
                ) {
                    indianStatesAndDistricts.keys.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                state = s
                                district = "" // reset district when state changes
                                expandedState = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // District Dropdown (Searchable)
            var expandedDistrict by remember { mutableStateOf(false) }
            val districtList = indianStatesAndDistricts[state] ?: emptyList()
            val filteredDistricts = if (district.isEmpty()) districtList else districtList.filter { it.contains(district, ignoreCase = true) }
            
            ExposedDropdownMenuBox(
                expanded = expandedDistrict,
                onExpandedChange = { if (districtList.isNotEmpty()) expandedDistrict = !expandedDistrict },
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                OutlinedTextField(
                    value = district,
                    onValueChange = { 
                        district = it
                        if (districtList.isNotEmpty()) expandedDistrict = true
                    },
                    readOnly = false,
                    label = { Text("District *", fontSize = 13.sp, color = Color.Black.copy(.7f)) },
                    leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = Purple, modifier = Modifier.size(20.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDistrict) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        focusedBorderColor = Purple, unfocusedBorderColor = Purple.copy(.25f),
                        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
                    )
                )
                if (filteredDistricts.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandedDistrict,
                        onDismissRequest = { expandedDistrict = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        filteredDistricts.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    district = d
                                    expandedDistrict = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // Taluk
            StyledTextField(
                value = taluk,
                onValueChange = { taluk = it },
                label = "Taluk / Mandal",
                icon = Icons.Default.Terrain,
                placeholder = "e.g. Kavuru Mandal"
            )
            Spacer(Modifier.height(14.dp))

            // Village/Street
            StyledTextField(
                value = villageAddress,
                onValueChange = { villageAddress = it },
                label = "Street Address / Village",
                icon = Icons.Default.Home,
                placeholder = "Full street address or village name",
                minLines = 2
            )

            Spacer(Modifier.height(36.dp))

            // ── Submit button ────────────────────────────────────────────────
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(
                    onClick = {
                        if (!isValid) return@Button
                        isLoading = true
                        
                        // Compile the full address
                        val fullAddressParts = listOf(villageAddress, taluk, district, state, "India").filter { it.isNotBlank() }
                        val fullAddress = fullAddressParts.joinToString(", ")
                        
                        val storeData = StoreData(
                            storeName = storeName.trim(),
                            storeDescription = storeDescription.trim(),
                            storeLogo = storeLogoUri,
                            storeLogoUrl = storeLogoUrl,
                            ownerName = ownerName.trim(),
                            gender = gender,
                            phoneNumber = phoneNumber.trim(),
                            address = fullAddress,
                            state = state.trim(),
                            district = district.trim(),
                            taluk = taluk.trim(),
                            storeProducts = selectedStoreProducts.toList() + if (showOthersField && customProductType.isNotBlank()) listOf(customProductType.trim()) else emptyList()
                        )
                        if (!isEditing) {
                            viewModel.createStore(context, storeData) { success ->
                                isLoading = false
                                if (success) onStoreCreated()
                            }
                        } else {
                            viewModel.updateStore(context, storeData)
                            isLoading = false
                            onStoreCreated()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple,
                        disabledContainerColor = Purple.copy(.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isEditing) Icons.Default.Save else Icons.Default.Launch,
                                null, tint = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (isEditing) "Save Changes" else "Launch My Store",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            if (!isValid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "* Store name, owner name, gender, phone, state, and district are required",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp)
                .background(Purple.copy(.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Purple, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E))
        Spacer(Modifier.weight(1f))
        Divider(
            modifier = Modifier.weight(2f),
            color = Purple.copy(.15f),
            thickness = 1.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp, color = Color.Black.copy(.7f)) },
        leadingIcon = {
            Icon(icon, null, tint = Purple, modifier = Modifier.size(20.dp))
        },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = Color.Gray.copy(.6f), fontSize = 13.sp) }
        } else null,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Purple,
            unfocusedBorderColor = Purple.copy(.25f),
            focusedLabelColor = Purple,
            unfocusedLabelColor = Color.Black.copy(.5f),
            focusedLeadingIconColor = Purple,
            unfocusedLeadingIconColor = Purple.copy(.6f),
            cursorColor = Purple
        )
    )
}