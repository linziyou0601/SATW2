package com.satw.demo.Controller;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.satw.demo.Dao.ProductRepository;
import com.satw.demo.Model.Msg;
import com.satw.demo.Model.Product;
import com.satw.demo.Model.User;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ManageProductController {
    
    @Autowired
    ProductRepository productRepository;
	
	@Autowired
    DataSource dataSource;

    String UPLOADED_FOLDER = System.getProperty("user.home") + File.separator + "uploads";

    //---------------------------------------我的產品---------------------------------------//
    //-------------------我的產品-------------------//
    @GetMapping("myProducts")
    public String myProducts(Model model, HttpSession session,  RedirectAttributes attr){
        User user = (User) session.getAttribute("user");
        if(user==null) return "redirect:/login?redirect=/myProducts";
        List<Product> products = productRepository.findBySeller(user);
        Collections.reverse(products);
        model.addAttribute("products", products);
        return "myProducts";
    }

    @PostMapping("myProducts/create")
    @ResponseBody
    public Msg requestCreateProduct(@RequestParam("title") String title,
                                    @RequestParam("description") String description,
                                    @RequestParam("price") String priceStr,
                                    @RequestParam("stockQty") String stockQtyStr,
                                    @RequestParam("files") MultipartFile files,
                                    HttpSession session
    ) {
        Msg msg;
        User user = (User) session.getAttribute("user");
        //驗證價格及數量是否合理
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            try {
                int price = Integer.parseInt(priceStr);
                int stockQty = Integer.parseInt(stockQtyStr);
                if(price<=0 || stockQty<0){
                    if(price<=0) msg = new Msg("Failed", "Price Must Large Than 0.", "error");
                    else msg = new Msg("Failed", "Stock Quantity Can Not Less Than 0.", "error");
                } else if(price>=Integer.MAX_VALUE || stockQty>=Integer.MAX_VALUE) {
                    if(price<=0) msg = new Msg("Failed", "Price Must Small Than "+Integer.MAX_VALUE+".", "error");
                    else msg = new Msg("Failed", "Stock Quantity Must Small Than "+Integer.MAX_VALUE+".", "error");
                } else {
                    //建立商品
                    Product product = new Product(user, title, description, price, null, stockQty);
                    product = productRepository.saveAndFlush(product);

                    //上傳圖片
                    String imgs = "/uploads/product_img.png";
                    if (!files.isEmpty()) {
                        try {
                            File file = new File(UPLOADED_FOLDER);
                            String extension = FilenameUtils.getExtension(files.getOriginalFilename());
                            Path path = Paths.get(file.getAbsolutePath() + File.separator + product.getId() + '.' + extension);
                            System.out.println(path);
                            byte[] bytes = files.getBytes();
                            OutputStream os = Files.newOutputStream(path);
                            os.write(bytes);
                            imgs = "/uploads/" + product.getId() + '.' + extension;
                            Files.write(path, bytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    product.setImgs(imgs);
                    productRepository.saveAndFlush(product);
                    msg = new Msg("Successful", "Product is created", "success");
                }
            } catch (NumberFormatException e){
                msg = new Msg("Error", "Invalid Number.", "error");
            }
        }
        return msg;
    }

    //-------------------產品編輯-------------------//
    @GetMapping("myProducts/{id}")
    public String editProduct(@PathVariable int id, Model model, HttpSession session, RedirectAttributes attr){
        User user = (User) session.getAttribute("user");
        if(user==null) return "redirect:/login?redirect=/myProducts/"+id;
        List<Product> products = productRepository.findById(id);
        if(products.size()>0 && products.get(0).getSeller().getId() == user.getId()){
            model.addAttribute("product", products.get(0));
            return "editProduct";
        } else {
            Msg msg = new Msg("Error", "Invalid operation!", "error");
            attr.addFlashAttribute("alert", msg);
            return "redirect:/myProducts";
        }
    }

    @PostMapping("myProducts/requestEditProduct")
    @ResponseBody
    public Msg requestEditProduct(@RequestParam("id") int id,
                                     @RequestParam("title") String title,
                                     @RequestParam("description") String description,
                                     @RequestParam("price") String priceStr,
                                     @RequestParam("stockQty") String stockQtyStr,
                                     @RequestParam("files") MultipartFile files,
                                     HttpSession session)
    {
        User user = (User) session.getAttribute("user");
        Msg msg = new Msg();
        List<Product> products = productRepository.findById(id);
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            //驗證是否可編輯
            if(products.size()>0 && products.get(0).getSeller().getId() == user.getId()){
                try {
                    int price = Integer.parseInt(priceStr);
                    int stockQty = Integer.parseInt(stockQtyStr);
                    Product product = products.get(0);
                    //驗證價格及數量是否合理
                    if(price<=0 || stockQty<0){
                        if(price<=0) msg = new Msg("Failed", "Price Must Large Than 0.", "error");
                        else msg = new Msg("Failed", "Stock Quantity Can Not Less Than 0.", "error");
                    } else {
                        product.setTitle(title);
                        product.setDescription(description);
                        product.setPrice(price);
                        product.setStockQty(stockQty);
                        productRepository.saveAndFlush(product);

                        //上傳圖片
                        String imgs = product.getImgs();
                        if (!files.isEmpty()) {
                            try {
                                //刪除舊檔
                                if(!imgs.equals("/uploads/product_img.png")){
                                    File file = new File(UPLOADED_FOLDER);
                                    Path fileToDeletePath = Paths.get(file.getAbsolutePath()+File.separator+imgs.substring(9));
                                    System.out.println(fileToDeletePath);
                                    Files.delete(fileToDeletePath);
                                }
                                //上傳新檔
                                File file = new File(UPLOADED_FOLDER);
                                String extension = FilenameUtils.getExtension(files.getOriginalFilename());
                                Path path = Paths.get(file.getAbsolutePath() + File.separator + product.getId() + '.' + extension);
                                System.out.println(path);
                                byte[] bytes = files.getBytes();
                                OutputStream os = Files.newOutputStream(path);
                                os.write(bytes);
                                imgs = "/uploads/" + product.getId() + '.' + extension;
                                Files.write(path, bytes);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        product.setImgs(imgs);
                        productRepository.saveAndFlush(product);
                        msg = new Msg("Successful", "Product is saved", "success");
                    }
                } catch(NumberFormatException e){
                    msg = new Msg("Error", "Invalid Number.", "error");
                }
            } else {
                msg = new Msg("Error", "Invalid operation!", "error");
            }
        }
        return msg;
    }

    @PostMapping("myProducts/requestDeleteProduct")
    @ResponseBody
    public Msg requestDeleteProduct(@RequestBody Map<String,String> reqMap, HttpSession session) {
        Msg msg = new Msg();
        User user = (User) session.getAttribute("user");
        List<Product> products = productRepository.findById(Integer.parseInt(reqMap.get("id")));
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            //驗證是否可編輯
            if(products.size()>0 && products.get(0).getSeller().getId() == user.getId()){
                Product product = products.get(0);
                product.setDeleted(true);
                productRepository.saveAndFlush(product);
                msg = new Msg("Successful", "Product is deleted", "success");
            } else {
                msg = new Msg("Error", "Invalid operation!", "error");
            }
        }
        return msg;
    }
}