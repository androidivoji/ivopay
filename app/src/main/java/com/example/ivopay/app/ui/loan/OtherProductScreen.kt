package com.example.ivopay.app.ui.loan

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ivopay.R
import com.example.ivopay.app.data.model.OtherProductItem
import com.example.ivopay.app.util.CommonUtils

@Composable
fun OtherProductScreen(
    viewModel: OtherProductViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // 1. Banner & List
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Banner Image
                    Image(
                        painter = painterResource(id = R.drawable.iv_hone_default_slider), // Placeholder
                        contentDescription = "Banner",
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        contentScale = ContentScale.FillWidth
                    )
                    
                    // Hot Tips Card (Absolute positioned in CSS, here at bottom of Box)
                    HotTipsCard(
                        totalBorrowers = viewModel.totalBorrowers,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp).offset(y = 30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(45.dp))
            }

            items(viewModel.productList) { product ->
                ProductItemCard(
                    item = product,
                    onApply = {
                        viewModel.jumpProduct(product.nik) { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // 2. Fixed Nav Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(22.dp).clickable { onBackClick() }
            )
            Text(text = "Limit pinjaman Anda", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            Box(modifier = Modifier.size(22.dp)) // Spacer
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
        }
    }
}

@Composable
fun HotTipsCard(totalBorrowers: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(Color(0xFFFEEFEF), Color.White)))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn), contentDescription = null, modifier = Modifier.size(20.dp)) // Placeholder
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Hari ini, ")
                        withStyle(style = SpanStyle(color = Color(0xFFFE5455), fontWeight = FontWeight.W500)) {
                            append(totalBorrowers.toString())
                        }
                        append(" nasabah telah berhasil menerima pencairan dana.")
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF262626)
                )
            }
        }
    }
}

@Composable
fun ProductItemCard(item: OtherProductItem, onApply: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Name + Tag + Borrower Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    AsyncImage(model = item.oic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = item.pam ?: "", fontSize = 14.sp, fontWeight = FontWeight.W500)
                    
                    // Tag / Subtext (oan)
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .background(Color(0xFFFFF4EC), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = item.oan ?: "", color = Color(0xFFFF7725), fontSize = 11.sp)
                    }
                }
                
                Text(
                    text = buildAnnotatedString {
                        append("Jumlah peminjam hari ini: ")
                        withStyle(style = SpanStyle(color = Color(0xFFFE5455))) {
                            append(item.borrowerCount.toString())
                        }
                    },
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

            // Body: Limit + Interest + Button
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Limit Maks (Rp)", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.6f))
                    Text(text = CommonUtils.formatRupiah(item.amo.toDouble()), fontSize = 20.sp, fontWeight = FontWeight.W600)
                }
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Bunga Harian", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.6f))
                    Text(text = "${item.ita}%", fontSize = 20.sp, fontWeight = FontWeight.W600)
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier.width(90.dp).height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF17CA69), Color(0xFF01B85C)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ajukan", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Period + Rating
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Loan period: ${item.ipe}-${item.ape}days", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.6f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.gad.toString(), fontSize = 12.sp, modifier = Modifier.padding(end = 5.dp))
                    RatingBar(rating = item.gad)
                }
            }
        }
    }
}

@Composable
fun RatingBar(rating: Float) {
    Row {
        repeat(5) { index ->
            val icon = if (index < rating.toInt()) Icons.Default.Star else Icons.Default.StarOutline
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFD14C),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
